package ext.vnua.veterinary_beapp.modules.pricing.service.impl;

import ext.vnua.veterinary_beapp.modules.pricing.dto.BrandPriceRowDto;
import ext.vnua.veterinary_beapp.modules.pricing.dto.PricingCalculateRequest;
import ext.vnua.veterinary_beapp.modules.pricing.ports.ProductBatchCalcPort;
import ext.vnua.veterinary_beapp.modules.pricing.ports.ProductionCostSheetPort;
import ext.vnua.veterinary_beapp.modules.pricing.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final ProductBatchCalcPort productBatchCalcPort;
    private final ProductionCostSheetPort productionCostSheetPort;

    private static final int SCALE_MONEY = 3;               // theo ví dụ 6,016 ; 8,656 ; 14,672...
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    @Override
    public List<BrandPriceRowDto> calculateBrandPrices(Long productId, PricingCalculateRequest request) {
        // 1) Lấy tổng amount & qtyKg từ service nội bộ
        var summary = productBatchCalcPort.calculateTotals(request.getFormulaId(), request.getBatchSizeKg());
        BigDecimal totalAmount = summary.getTotalAmount();
        BigDecimal qtyKg = summary.getQtyKg();

        if (totalAmount == null || qtyKg == null) {
            throw new IllegalStateException("Thiếu dữ liệu calc (totalAmount/qtyKg).");
        }

        List<BrandPriceRowDto> rows = new ArrayList<>();
        int idx = 1;

        // 2) Tính cho từng brand
        for (PricingCalculateRequest.BrandInput b : request.getBrands()) {
            // cpnl = amount/qtyKg/1000/packSize
            BigDecimal cpnl = totalAmount
                    .divide(qtyKg, SCALE_MONEY + 6, RM)
                    .divide(new BigDecimal("1000"), SCALE_MONEY + 6, RM)
                    .divide(b.getPackSize(), SCALE_MONEY, RM);

            // cpsx = unitCost theo sheetCode
            BigDecimal cpsx = productionCostSheetPort.getUnitCostBySheetCode(b.getSheetCode())
                    .setScale(SCALE_MONEY, RM);

            BigDecimal cost = cpnl.add(cpsx).setScale(SCALE_MONEY, RM);
            BigDecimal salePrice = cost.multiply(BigDecimal.ONE.add(b.getProfitPercent())).setScale(SCALE_MONEY, RM);

            rows.add(BrandPriceRowDto.builder()
                    .stt(idx++)
                    .manualCode(b.getManualCode())
                    .packSize(b.getPackSize())
                    .specName(b.getSpecName())
                    .cpnl(cpnl)
                    .cpsx(cpsx)
                    .cost(cost)
                    .profitPercent(b.getProfitPercent())
                    .salePrice(salePrice)
                    .build());
        }

        return rows;
    }
}
