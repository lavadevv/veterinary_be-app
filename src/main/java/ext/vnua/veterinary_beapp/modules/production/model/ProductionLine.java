package ext.vnua.veterinary_beapp.modules.production.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.production.enums.ProductionLineStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "production_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
// Dây chuyền sản xuất.
public class ProductionLine extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "line_code", nullable = false, unique = true, length = 50)
    private String lineCode; // Mã dây chuyền (LINE-01)

    @Column(name = "name", nullable = false, length = 255)
    private String name; // Tên dây chuyền

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", length = 50)
    private String status; // ACTIVE / INACTIVE / UNDER_MAINTENANCE

    // Helper methods
    public ProductionLineStatus getStatusEnum() {
        return ProductionLineStatus.fromCode(this.status);
    }

    public void setStatusEnum(ProductionLineStatus status) {
        this.status = status.getCode();
    }

    public boolean isActive() {
        return ProductionLineStatus.ACTIVE.getCode().equals(this.status);
    }

    public boolean isInactive() {
        return ProductionLineStatus.INACTIVE.getCode().equals(this.status);
    }

    public boolean isUnderMaintenance() {
        return ProductionLineStatus.UNDER_MAINTENANCE.getCode().equals(this.status);
    }
}