package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.enums.AllocationMethod;
import ext.vnua.veterinary_beapp.modules.material.enums.OverheadType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "overhead_costs", indexes = {
        @Index(name = "idx_ohc_date", columnList = "cost_date"),
        @Index(name = "idx_ohc_period", columnList = "period_month"),
        @Index(name = "idx_ohc_type", columnList = "type")
})
@Data @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OverheadCost extends AuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã chi phí: ví dụ cuocvc1, bốc xếp… (tuỳ chọn) */
    @Column(name = "code", length = 50)
    private String code;

    /** Tên/diễn giải ngắn gọn: “Cước vận chuyển Nan Hải”, “Nhân công đóng gói”… */
    @Column(name = "title", length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OverheadType type = OverheadType.KHAC;

    /** Ngày chứng từ/ghi nhận */
    @Column(name = "cost_date", nullable = false)
    private LocalDate costDate = LocalDate.now();

    /** Kỳ hạch toán (ngày 01 của tháng) – phục vụ filter theo tháng */
    @Column(name = "period_month", nullable = false)
    private LocalDate periodMonth = LocalDate.now().withDayOfMonth(1);

    /** Đơn vị tính hiển thị: “lần”, “giờ”, “cái”, “kg”… */
    @Column(name = "uom", length = 30)
    private String unitOfMeasure;

    /** Số lượng (mặc định 1) */
    @Column(name = "quantity", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantity = BigDecimal.ONE;

    /** Đơn giá (VNĐ) */
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    /** Thành tiền = quantity * unitPrice (auto tính) */
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_allocation", nullable = false)
    private AllocationMethod suggestedAllocation = AllocationMethod.KHAC;

    @Column(name = "note", length = 2000)
    private String note;

    @Column(name = "ref_no", length = 100)
    private String refNo;

    @Column(name = "cost_center", length = 100)
    private String costCenter;

    @Column(name = "product_id")
    private Long productId; // optional tag

    @Column(name = "product_batch_id")
    private Long productBatchId; // optional tag

    @PrePersist
    @PreUpdate
    private void preCalc() {
        if (costDate != null) periodMonth = costDate.withDayOfMonth(1);
        if (quantity == null) quantity = BigDecimal.ONE;
        if (unitPrice == null) unitPrice = BigDecimal.ZERO;
        amount = unitPrice.multiply(quantity);
        if (unitOfMeasure == null || unitOfMeasure.isBlank()) unitOfMeasure = "lần";
        if (title == null || title.isBlank()) title = "Chi phí SXC";
    }
}
