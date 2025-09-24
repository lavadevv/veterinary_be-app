package ext.vnua.veterinary_beapp.modules.production.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.production.enums.IssueStatus;
import ext.vnua.veterinary_beapp.modules.production.enums.IssueType;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "production_order_issues", indexes = {
        @Index(name = "idx_issue_order", columnList = "production_order_id"),
        @Index(name = "idx_issue_type", columnList = "issue_type"),
        @Index(name = "idx_issue_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
// Phiếu cấp phát nguyên liệu / bao bì gắn với lệnh sản xuất
public class ProductionOrderIssue extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false, length = 30)
    private IssueType issueType; // MATERIAL / PACKAGING

    @Column(name = "issue_code", unique = true, length = 50)
    private String issueCode; // Ví dụ: ISS-20250914-001

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private IssueStatus status = IssueStatus.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy; // Người duyệt phiếu

    // Helper methods
    public boolean isPending() {
        return IssueStatus.PENDING.equals(this.status);
    }

    public boolean isCompleted() {
        return IssueStatus.COMPLETED.equals(this.status);
    }

    public boolean isCancelled() {
        return IssueStatus.CANCELLED.equals(this.status);
    }
}
