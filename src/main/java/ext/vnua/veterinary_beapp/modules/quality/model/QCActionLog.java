package ext.vnua.veterinary_beapp.modules.quality.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.quality.enums.QCStatus;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "qc_action_logs", indexes = {
        @Index(name = "idx_qc_action_log_record", columnList = "qc_record_id"),
        @Index(name = "idx_qc_action_log_user", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QCActionLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qc_record_id", nullable = false)
    private ProductionQCRecord qcRecord;

    @Column(nullable = false, length = 50)
    private String action; // CREATE, UPDATE, REVIEW, APPROVE, REJECT, RETEST

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private QCStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 30)
    private QCStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
