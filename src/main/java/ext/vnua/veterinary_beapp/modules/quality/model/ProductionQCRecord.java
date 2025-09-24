package ext.vnua.veterinary_beapp.modules.quality.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBatch;
import ext.vnua.veterinary_beapp.modules.quality.enums.*;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_qc_records", indexes = {
        @Index(name = "idx_qc_record_order", columnList = "production_order_id"),
        @Index(name = "idx_qc_record_standard", columnList = "qc_standard_id"),
        @Index(name = "idx_qc_record_status", columnList = "status"),
        @Index(name = "idx_qc_record_result", columnList = "result")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductionQCRecord extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_number", unique = true, nullable = false, length = 50)
    private String recordNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id")
    private ProductionOrder productionOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_batch_id")
    private ProductBatch productBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_batch_id")
    private MaterialBatch materialBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qc_standard_id", nullable = false)
    private QCStandard qcStandard;

    @Enumerated(EnumType.STRING)
    @Column(name = "qc_type", nullable = false, length = 30)
    private QCType qcType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QCStatus status = QCStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 30)
    private QCResult result = QCResult.PENDING;

    @Column(name = "inspection_date", nullable = false)
    private LocalDateTime inspectionDate;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id", nullable = false)
    private User inspector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column(columnDefinition = "TEXT")
    private String overallNotes;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_action", length = 30)
    private QCFailureAction failureAction;

    @Column(name = "batch_size", precision = 15, scale = 3)
    private BigDecimal batchSize;

    @Column(name = "sample_size", precision = 15, scale = 3)
    private BigDecimal sampleSize;

    @Column(name = "sample_info", columnDefinition = "TEXT")
    private String sampleInfo;

    @Column(columnDefinition = "TEXT")
    private String attachments;

    @OneToMany(mappedBy = "qcRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QCTestResult> testResults = new ArrayList<>();

    @OneToMany(mappedBy = "qcRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QCActionLog> actionLogs = new ArrayList<>();
}
