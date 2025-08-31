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
}

