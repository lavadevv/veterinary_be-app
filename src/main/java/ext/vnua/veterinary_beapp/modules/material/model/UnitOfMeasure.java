// ext/vnua/veterinary_beapp/modules/material/model/UnitOfMeasure.java
package ext.vnua.veterinary_beapp.modules.material.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "unit_of_measures",
        uniqueConstraints = @UniqueConstraint(name = "uk_uom_name", columnNames = "name"))
@Getter @Setter
public class UnitOfMeasure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable=false, length=255)
    private String name;   // VD: Kilogram, Gram, Mililiter...
}
