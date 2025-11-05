// File: ext/vnua/veterinary_beapp/modules/product/model/FormulaHeader.java
package ext.vnua.veterinary_beapp.modules.product.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "formula_headers",
        indexes = {
                @Index(name = "idx_formula_code", columnList = "formula_code", unique = true),
                @Index(name = "idx_formula_header_active", columnList = "is_active")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FormulaHeader extends AuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "formula_code", nullable = false, length = 64, unique = true)
    private String formulaCode;          // Mã công thức (bắt buộc)

    @Column(name = "formula_name", length = 255)
    private String formulaName;          // Tên công thức (tuỳ chọn)

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    // Dùng chung cho nhiều sản phẩm
    @ManyToMany
    @JoinTable(name = "formula_header_products",
            joinColumns = @JoinColumn(name = "header_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<ext.vnua.veterinary_beapp.modules.product.model.Product> products = new HashSet<>();
}
