package ext.vnua.veterinary_beapp.modules.quality.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "qc_alerts", indexes = {
        @Index(name = "idx_qc_alert_record", columnList = "qc_record_id"),
        @Index(name = "idx_qc_alert_product", columnList = "product_id"),
        @Index(name = "idx_qc_alert_resolved", columnList = "is_resolved")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QCAlert extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "qc_record_id")
    private Long qcRecordId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    public enum AlertType {
        OVERDUE, CRITICAL_FAIL, EQUIPMENT, TREND
    }

    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
