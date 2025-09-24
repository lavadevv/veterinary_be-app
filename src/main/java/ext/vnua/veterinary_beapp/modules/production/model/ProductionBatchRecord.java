package ext.vnua.veterinary_beapp.modules.production.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.production.enums.RecordStatus;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_batch_records", indexes = {
        @Index(name = "idx_batch_record_order", columnList = "production_order_id"),
        @Index(name = "idx_batch_record_date", columnList = "record_date"),
        @Index(name = "idx_batch_record_step", columnList = "step_name"),
        @Index(name = "idx_batch_record_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
// Hồ sơ lô sản xuất, phục vụ truy xuất nguồn gốc
public class ProductionBatchRecord extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName; // Ví dụ: Chuẩn bị nguyên liệu, IPC kiểm tra, QC, Đóng gói…

    @Column(name = "result", columnDefinition = "TEXT")
    private String result; // Kết quả bước kiểm tra

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy; // Người duyệt bước này

    @Column(name = "approved_date")
    private LocalDateTime approvedDate; // Ngày duyệt

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status = RecordStatus.PENDING; // PENDING, APPROVED, REJECTED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Ghi chú thêm

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments; // JSON string chứa danh sách file đính kèm

    @Column(name = "temperature")
    private Double temperature; // Nhiệt độ (nếu cần)

    @Column(name = "humidity")
    private Double humidity; // Độ ẩm (nếu cần)

    @Column(name = "pressure")
    private Double pressure; // Áp suất (nếu cần)

    @Column(name = "duration_minutes")
    private Integer durationMinutes; // Thời gian thực hiện bước (phút)

    @Column(name = "sequence_number")
    private Integer sequenceNumber; // Số thứ tự trong quy trình

    // Business methods
    public boolean isApproved() {
        return RecordStatus.APPROVED.equals(this.status);
    }

    public boolean isPending() {
        return RecordStatus.PENDING.equals(this.status);
    }

    public boolean isRejected() {
        return RecordStatus.REJECTED.equals(this.status);
    }

    public void approve(User approver) {
        this.approvedBy = approver;
        this.approvedDate = LocalDateTime.now();
        this.status = RecordStatus.APPROVED;
    }

    public void reject(User reviewer, String reason) {
        this.approvedBy = reviewer;
        this.approvedDate = LocalDateTime.now();
        this.status = RecordStatus.REJECTED;
        this.notes = (this.notes != null ? this.notes + "\n" : "") + "Lý do từ chối: " + reason;
    }
}