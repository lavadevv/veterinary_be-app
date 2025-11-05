package ext.vnua.veterinary_beapp.modules.material.model;


import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "material_active_ingredients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MaterialActiveIngredient extends AuditableEntity {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY, optional = false)
     @JoinColumn(name = "material_id",
         foreignKey = @ForeignKey(name = "fk_mai_material"))
     private Material material;

     @ManyToOne(fetch = FetchType.LAZY, optional = false)
     @JoinColumn(name = "active_ingredient_id",
              foreignKey = @ForeignKey(name = "fk_mai_active_ingredient"))
     private ActiveIngredient activeIngredient;

     // Giá trị & đơn vị: ví dụ 80 (%) hoặc 100000 (IU/g) hoặc 500 (mg/g)
     @Column(name = "content_value", precision = 18, scale = 6)
     private BigDecimal contentValue;

     @Column(name = "content_unit", length = 30)
     private String contentUnit; // "%", "IU/g", "mg/g", ...

     @Column(name = "notes", columnDefinition = "TEXT")
     private String notes;
}