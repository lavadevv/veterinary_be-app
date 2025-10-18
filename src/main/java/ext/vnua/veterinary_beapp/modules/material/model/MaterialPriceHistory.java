// ext/vnua/veterinary_beapp/modules/material/model/MaterialPriceHistory.java
package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "material_price_histories", indexes = {
        @Index(name = "idx_mph_material", columnList = "material_id"),
        @Index(name = "idx_mph_effective_date", columnList = "effective_date")
})
@Data @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MaterialPriceHistory extends AuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "price", precision = 15, scale = 2, nullable = false)
    private BigDecimal price; // "giá công thức" tại thời điểm thay đổi

    @Column(name = "uom", length = 20)
    private String uom; // g hoặc kg (tùy material.unitOfMeasure lúc đó)

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate = LocalDate.now(); // ngày bắt đầu hiệu lực

    @Column(name = "note", length = 1000)
    private String note;
}
