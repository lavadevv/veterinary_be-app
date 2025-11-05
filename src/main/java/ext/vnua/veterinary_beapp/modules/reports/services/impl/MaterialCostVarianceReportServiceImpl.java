package ext.vnua.veterinary_beapp.modules.reports.services.impl;

import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialPriceHistory;
import ext.vnua.veterinary_beapp.modules.material.model.OverheadCost;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialPriceHistoryRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.OverheadCostRepository;
import ext.vnua.veterinary_beapp.modules.reports.dto.MaterialCostVarianceRequest;
import ext.vnua.veterinary_beapp.modules.reports.dto.MaterialCostVarianceResponse;
import ext.vnua.veterinary_beapp.modules.reports.services.MaterialCostVarianceReportService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialCostVarianceReportServiceImpl implements MaterialCostVarianceReportService {

    private final MaterialBatchRepository materialBatchRepo;
    private final OverheadCostRepository overheadRepo;
    // Nếu bạn CHƯA có module history, vẫn giữ bean này là optional bằng @Autowired(required=false)
    // hoặc tạo repo dummy. Ở đây giả định bạn đã có.
    private final MaterialPriceHistoryRepository priceHistoryRepo;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public MaterialCostVarianceResponse buildReport(MaterialCostVarianceRequest req, boolean includeOverheads) {
        int year = req.getYear();
        int month = req.getMonth();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        List<MaterialCostVarianceResponse.Row> rows = new ArrayList<>();

        // 1) Hàng nhập kho (MaterialBatch) - Updated for MaterialBatchItem structure
        List<MaterialBatch> batches = materialBatchRepo.findByReceivedDateBetween(from, to);
        for (MaterialBatch mb : batches) {
            // MaterialBatch now contains multiple items, iterate through each item
            if (mb.getBatchItems() == null || mb.getBatchItems().isEmpty()) {
                continue; // Skip empty batches
            }
            
            for (var item : mb.getBatchItems()) {
                Material m = item.getMaterial();
                if (m == null) continue;
                
                BigDecimal qty = nvl(item.getReceivedQuantity());
                BigDecimal unitPrice = nvl(item.getUnitPrice()); // giá nhập theo item
                BigDecimal formulaPrice = resolveFormulaPrice(m.getId(), mb.getReceivedDate(), m); // giá công thức
                BigDecimal variance = (formulaPrice == null ? null : formulaPrice.subtract(unitPrice));
                BigDecimal varianceAmt = (variance == null ? BigDecimal.ZERO : variance.multiply(qty));
                BigDecimal total = unitPrice.multiply(qty);

                MaterialCostVarianceResponse.Row row = new MaterialCostVarianceResponse.Row();
                row.setDate(mb.getReceivedDate());
                row.setCode(m.getMaterialCode());
                row.setItemName(m.getMaterialName());
                var uom = m.getUnitOfMeasure();
                String uomName = (uom != null) ? uom.getName() : "N/A";
                row.setUom(safeLower(uomName)); // g/kg/…
                row.setQuantity(scale(qty, 3));
                row.setUnitPrice(scale(unitPrice, 2));
                row.setFormulaPrice(formulaPrice == null ? null : scale(formulaPrice, 2));
                row.setVariance(variance == null ? null : scale(variance, 2));
                row.setVarianceAmount(scale(varianceAmt, 2));
                row.setNotes(item.getNotes() != null ? item.getNotes() : mb.getNotes());
                row.setTotalAmount(scale(total, 2));
                row.setSourceType("MATERIAL_BATCH_ITEM");
                row.setSourceId(item.getId());
                rows.add(row);
            }
        }

        // 2) Chi phí ngoài (Overhead) – hiển thị như dòng “OTHER_COST”
        if (includeOverheads) {
            LocalDate periodMonth = from.withDayOfMonth(1);
            List<OverheadCost> ohcs = overheadRepo.findByPeriodMonthOrderByCostDateDesc(periodMonth);

            for (OverheadCost oc : ohcs) {
                BigDecimal amount = nvl(oc.getAmount());      // giá nhập (đúng hơn là “giá chi phí”)
                BigDecimal qty = BigDecimal.ONE;              // đơn vị “lần”
                BigDecimal total = amount;
                // Theo yêu cầu: chênh = (formulaPrice - unitPrice). Ở chi phí khác: formulaPrice = null → thể hiện số âm
                // Ta set variance = -amount để FE render (…)
                BigDecimal variance = amount.negate();
                BigDecimal varianceAmt = variance.multiply(qty);

                MaterialCostVarianceResponse.Row row = new MaterialCostVarianceResponse.Row();
                row.setDate(oc.getCostDate());
                row.setCode(oc.getType().name());     // hoặc oc.getRefNo()
                row.setItemName(safeNote(oc));
                row.setUom("lần");
                row.setQuantity(BigDecimal.ONE);
                row.setUnitPrice(scale(amount, 2));
                row.setFormulaPrice(null);
                row.setVariance(scale(variance, 2));          // âm
                row.setVarianceAmount(scale(varianceAmt, 2)); // âm
                row.setNotes(oc.getRefNo());
                row.setTotalAmount(scale(total, 2));
                row.setSourceType("OTHER_COST");
                row.setSourceId(oc.getId());
                rows.add(row);
            }
        }

        // 3) Totals
        BigDecimal totalPurchase = BigDecimal.ZERO;
        BigDecimal totalVariance = BigDecimal.ZERO;
        for (var r : rows) {
            totalPurchase = totalPurchase.add(nvl(r.getTotalAmount()));
            totalVariance = totalVariance.add(nvl(r.getVarianceAmount()));
        }

        MaterialCostVarianceResponse.Totals totals = new MaterialCostVarianceResponse.Totals(
                scale(totalPurchase, 2),
                scale(totalVariance, 2),
                rows.size()
        );

        return new MaterialCostVarianceResponse(year, month, rows, totals);
    }

    /** Tìm giá công thức theo lịch sử; fallback về Material.fixedPrice nếu không có. */
    private BigDecimal resolveFormulaPrice(Long materialId, LocalDate date, Material mFallback) {
        // Tìm bản ghi history gần nhất (<= date)
        MaterialPriceHistory h = priceHistoryRepo
                .findTopByMaterialIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(materialId, date)
                .orElse(null);
        if (h != null && h.getPrice() != null) {
            return scale(h.getPrice(), 2);
        }
        // fallback về fixedPrice trong Material
        if (mFallback != null && mFallback.getFixedPrice() != null) {
            return scale(mFallback.getFixedPrice(), 2);
        }
        return null; // không có giá công thức
    }

    private static BigDecimal nvl(BigDecimal x) { return x == null ? BigDecimal.ZERO : x; }
    private static BigDecimal scale(BigDecimal x, int s) { return x == null ? null : x.setScale(s, RoundingMode.HALF_UP); }
    private static String safeLower(String s) { return s == null ? null : s.trim(); }
    private static String safeNote(OverheadCost oc) {
        if (oc.getNote() != null && !oc.getNote().isBlank()) return oc.getNote();
        return "Chi phí ngoài (" + oc.getType().name() + ")";
    }
}
