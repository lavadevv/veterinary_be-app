package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "material_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MaterialTransaction extends AuditableEntity {

    public enum TransactionType {
        NHAP_KHO("Nhập kho"),
        XUAT_KHO("Xuất kho"),
        DIEU_CHINH("Điều chỉnh kiểm kê"),
        TRA_HANG("Trả hàng"),
        HUY_BO("Hủy bỏ");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_batch_id", nullable = false)
    private MaterialBatch materialBatch;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "reference_document")
    private String referenceDocument; // Số phiếu nhập/xuất, hóa đơn...

    @Column(name = "production_order_id")
    private String productionOrderId; // Link to production order if applicable

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_location_id")
    private Location fromLocation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_location_id")
    private Location toLocation;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== GIAO DỊCH NGUYÊN LIỆU ===\n");
        sb.append("Loại giao dịch       : ").append(transactionType.getDisplayName()).append("\n");
        sb.append("Ngày giao dịch       : ").append(transactionDate).append("\n");
        sb.append("Nguyên liệu (Lô)     : ").append(materialBatch != null ? materialBatch.getBatchNumber() : "Không rõ").append("\n");
        sb.append("Số lượng             : ").append(quantity).append(" ").append(materialBatch.getMaterial().getUnitOfMeasure()).append("\n");
        if (unitPrice != null) sb.append("Đơn giá              : ").append(unitPrice).append(" VND\n");
        if (totalValue != null) sb.append("Thành tiền           : ").append(totalValue).append(" VND\n");
        if (referenceDocument != null) sb.append("Chứng từ tham chiếu  : ").append(referenceDocument).append("\n");
        if (productionOrderId != null) sb.append("Lệnh sản xuất liên quan: ").append(productionOrderId).append("\n");
        if (fromLocation != null) sb.append("Từ kho               : ").append(fromLocation.getLocationCode()).append("\n");
        if (toLocation != null) sb.append("Đến kho              : ").append(toLocation.getLocationCode()).append("\n");
        if (reason != null) sb.append("Lý do                : ").append(reason).append("\n");
        if (notes != null) sb.append("Ghi chú              : ").append(notes).append("\n");
        if (approvedBy != null) sb.append("Người duyệt          : ").append(approvedBy.getFullName()).append("\n");
        return sb.toString();
    }


}
