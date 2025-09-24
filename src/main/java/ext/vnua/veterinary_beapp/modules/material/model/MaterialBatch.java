package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "material_batches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MaterialBatch extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber; // Mã lô nhà sản xuất

    @Column(name = "internal_batch_code", unique = true)
    private String internalBatchCode; // Mã lô nội bộ

    @Column(name = "manufacturer_batch_number")
    private String manufacturerBatchNumber;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(name = "received_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal receivedQuantity;

    @Column(name = "current_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal currentQuantity;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_status", nullable = false)
    private TestStatus testStatus = TestStatus.CHO_KIEM_NGHIEM;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_status", nullable = false)
    private UsageStatus usageStatus = UsageStatus.CACH_LY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "coa_number")
    private String coaNumber; // Certificate of Analysis number

    @Column(name = "test_report_number")
    private String testReportNumber;

    @Column(name = "test_results", columnDefinition = "JSON")
    private String testResults; // JSON format for test parameters

    @Column(name = "quarantine_reason", columnDefinition = "TEXT")
    private String quarantineReason;

    @Column(name = "coa_file_path")
    private String coaFilePath;

    @Column(name = "msds_file_path")
    private String msdsFilePath;

    @Column(name = "test_certificate_path")
    private String testCertificatePath;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== THÔNG TIN LÔ NGUYÊN LIỆU ===\n");
        sb.append("Mã lô nội bộ         : ").append(internalBatchCode != null ? internalBatchCode : "Chưa có").append("\n");
        sb.append("Mã lô NSX            : ").append(batchNumber).append("\n");
        if (manufacturerBatchNumber != null) sb.append("Mã lô của hãng       : ").append(manufacturerBatchNumber).append("\n");
        if (manufacturingDate != null) sb.append("Ngày sản xuất        : ").append(manufacturingDate).append("\n");
        if (expiryDate != null) sb.append("Hạn sử dụng          : ").append(expiryDate).append("\n");
        sb.append("Ngày nhập            : ").append(receivedDate).append("\n");
        sb.append("Số lượng nhập        : ").append(receivedQuantity).append(" ").append(material.getUnitOfMeasure()).append("\n");
        sb.append("Số lượng hiện tại    : ").append(currentQuantity).append(" ").append(material.getUnitOfMeasure()).append("\n");
        if (unitPrice != null) sb.append("Đơn giá              : ").append(unitPrice).append(" VND\n");
        sb.append("Tình trạng kiểm nghiệm: ").append(testStatus.getDisplayName()).append("\n");
        sb.append("Trạng thái sử dụng   : ").append(usageStatus.getDisplayName()).append("\n");
        if (location != null) sb.append("Vị trí kho           : ").append(location.getLocationCode()).append("\n");
        if (coaNumber != null) sb.append("Số COA               : ").append(coaNumber).append("\n");
        if (testReportNumber != null) sb.append("Số báo cáo kiểm nghiệm: ").append(testReportNumber).append("\n");
        if (quarantineReason != null) sb.append("Lý do cách ly        : ").append(quarantineReason).append("\n");
        if (notes != null) sb.append("Ghi chú              : ").append(notes).append("\n");
        return sb.toString();
    }

    // thêm 2 trường bổ sung để theo dõi chi tiết hơn
    @Column(name = "reserved_quantity", precision = 15, scale = 3)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "available_quantity", precision = 15, scale = 3)
    private BigDecimal availableQuantity;


    // trong class MaterialBatch
    @PrePersist
    public void prePersist() {
        if (reservedQuantity == null) reservedQuantity = java.math.BigDecimal.ZERO;
        if (availableQuantity == null) {
            // nếu chưa set, mặc định available = current - reserved (>= 0)
            availableQuantity = (currentQuantity != null ? currentQuantity : java.math.BigDecimal.ZERO)
                    .subtract(reservedQuantity);
            if (availableQuantity.signum() < 0) availableQuantity = java.math.BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (reservedQuantity == null) reservedQuantity = java.math.BigDecimal.ZERO;
        if (availableQuantity == null) availableQuantity = java.math.BigDecimal.ZERO;
        // đảm bảo không âm
        if (availableQuantity.signum() < 0) availableQuantity = java.math.BigDecimal.ZERO;
    }


}

