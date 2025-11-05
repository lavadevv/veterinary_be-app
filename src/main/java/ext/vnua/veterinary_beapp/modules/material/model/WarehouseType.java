package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "warehouse_types",
        uniqueConstraints = @UniqueConstraint(name = "uk_warehouse_types_name", columnNames = {"name"})
)
public class WarehouseType extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên loại kho (ví dụ: Kho nguyên liệu, Kho bao bì, Kho thành phẩm) */
    @Column(name = "name", nullable = false, length = 255)
    private String name;
}
