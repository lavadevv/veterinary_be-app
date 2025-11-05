// File: ext/vnua/veterinary_beapp/modules/material/model/MaterialFormType.java
package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "material_form_types",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_material_form_types_name", columnNames = "form_name")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MaterialFormType extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ví dụ: "Bột", "Lỏng", "Hạt", ... */
    @Column(name = "form_name", nullable = false, length = 100)
    private String name;
}
