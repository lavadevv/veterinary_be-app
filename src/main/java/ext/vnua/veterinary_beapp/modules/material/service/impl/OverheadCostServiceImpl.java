package ext.vnua.veterinary_beapp.modules.material.service.impl;


import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.request.cost.CreateOverheadCostRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.cost.UpdateOverheadCostRequest;
import ext.vnua.veterinary_beapp.modules.material.model.OverheadCost;
import ext.vnua.veterinary_beapp.modules.material.repository.OverheadCostRepository;
import ext.vnua.veterinary_beapp.modules.material.service.OverheadCostService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OverheadCostServiceImpl implements OverheadCostService {

    private final OverheadCostRepository repo;

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private static LocalDate normMonth(LocalDate d) { return (d == null ? LocalDate.now() : d).withDayOfMonth(1); }

    private static BigDecimal calcAmount(BigDecimal qty, BigDecimal price, BigDecimal fallback) {
        // Nếu có đủ qty & price thì tính, ngược lại dùng fallback (có thể = 0)
        if (qty != null && price != null) {
            return qty.multiply(price).setScale(2, RoundingMode.HALF_UP);
        }
        return nz(fallback).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "OverheadCost", description = "Thêm chi phí ngoài")
    public OverheadCost create(CreateOverheadCostRequest req) {
        OverheadCost e = new OverheadCost();

        // Thông tin cơ bản
        LocalDate costDate = (req.getCostDate() != null) ? req.getCostDate() : LocalDate.now();
        e.setCostDate(costDate);
        e.setPeriodMonth(normMonth(req.getPeriodMonth() != null ? req.getPeriodMonth() : costDate));
        e.setType(req.getType());

        // Diễn giải, phân loại
        e.setCode(req.getCode());
        e.setTitle(req.getTitle());
        e.setRefNo(req.getRefNo());
        e.setCostCenter(req.getCostCenter());
        e.setNote(req.getNote());
        e.setSuggestedAllocation(req.getSuggestedAllocation());

        // Gắn tag (nếu có)
        e.setProductId(req.getProductId());
        e.setProductBatchId(req.getProductBatchId());

        // Đơn vị – số lượng – đơn giá – thành tiền
        e.setUnitOfMeasure(req.getUnitOfMeasure() == null || req.getUnitOfMeasure().isBlank() ? "lần" : req.getUnitOfMeasure());
        e.setQuantity(req.getQuantity() == null ? BigDecimal.ONE : req.getQuantity());
        e.setUnitPrice(req.getUnitPrice()); // có thể null

        // amount: ưu tiên tự tính từ qty*price; nếu thiếu 1 trong 2 thì fallback sang req.amount
        BigDecimal amount = calcAmount(req.getQuantity(), req.getUnitPrice(), req.getAmount());
        e.setAmount(amount);

        return repo.saveAndFlush(e);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "OverheadCost", description = "Sửa chi phí ngoài")
    public OverheadCost update(UpdateOverheadCostRequest req) {
        OverheadCost e = repo.findById(req.getId())
                .orElseThrow(() -> new DataExistException("Chi phí không tồn tại"));

        // Cập nhật thông tin cơ bản
        if (req.getCostDate() != null) {
            e.setCostDate(req.getCostDate());
            // đồng bộ periodMonth theo costDate nếu client không gửi periodMonth riêng
            if (req.getPeriodMonth() == null) {
                e.setPeriodMonth(req.getCostDate().withDayOfMonth(1));
            }
        }
        if (req.getPeriodMonth() != null) {
            e.setPeriodMonth(normMonth(req.getPeriodMonth()));
        }
        if (req.getType() != null) e.setType(req.getType());

        // Diễn giải, phân loại
        if (req.getCode() != null) e.setCode(req.getCode());
        if (req.getTitle() != null) e.setTitle(req.getTitle());
        if (req.getRefNo() != null) e.setRefNo(req.getRefNo());
        if (req.getCostCenter() != null) e.setCostCenter(req.getCostCenter());
        if (req.getNote() != null) e.setNote(req.getNote());
        if (req.getSuggestedAllocation() != null) e.setSuggestedAllocation(req.getSuggestedAllocation());

        // Gắn tag (nếu có)
        if (req.getProductId() != null) e.setProductId(req.getProductId());
        if (req.getProductBatchId() != null) e.setProductBatchId(req.getProductBatchId());

        // Đơn vị – số lượng – đơn giá
        if (req.getUnitOfMeasure() != null) e.setUnitOfMeasure(req.getUnitOfMeasure());
        if (req.getQuantity() != null) e.setQuantity(req.getQuantity());
        if (req.getUnitPrice() != null) e.setUnitPrice(req.getUnitPrice());

        // Tính lại amount nếu client có thay đổi qty/price; nếu không, cho phép set amount trực tiếp
        boolean qtyChanged = req.getQuantity() != null;
        boolean priceChanged = req.getUnitPrice() != null;
        if (qtyChanged || priceChanged) {
            BigDecimal qty = qtyChanged ? req.getQuantity() : e.getQuantity();
            BigDecimal price = priceChanged ? req.getUnitPrice() : e.getUnitPrice();
            e.setAmount(calcAmount(qty, price, req.getAmount()));
        } else if (req.getAmount() != null) {
            e.setAmount(req.getAmount().setScale(2, RoundingMode.HALF_UP));
        }

        return repo.saveAndFlush(e);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "OverheadCost", description = "Xoá chi phí ngoài")
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public OverheadCost get(Long id) {
        return repo.findById(id).orElseThrow(() -> new DataExistException("Chi phí không tồn tại"));
    }

    @Override
    public List<OverheadCost> listByDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            // nếu không truyền, mặc định tháng hiện tại
            LocalDate now = LocalDate.now();
            from = now.withDayOfMonth(1);
            to = now.withDayOfMonth(now.lengthOfMonth());
        }
        return repo.findByCostDateBetweenOrderByCostDateDesc(from, to);
    }

    @Override
    public List<OverheadCost> listByPeriod(LocalDate periodMonth) {
        LocalDate pm = normMonth(periodMonth);
        return repo.findByPeriodMonthOrderByCostDateDesc(pm);
    }
}
