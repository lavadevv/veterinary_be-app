package ext.vnua.veterinary_beapp.modules.product.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_formulas", indexes = {
        @Index(name = "idx_formula_product", columnList = "product_id"),
        @Index(name = "idx_formula_active", columnList = "is_active"),
        @Index(name = "idx_formula_version", columnList = "version")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductFormula extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "batch_size", precision = 10, scale = 3)
    private BigDecimal batchSize;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "sop_file_path")
    private String sopFilePath;

    /* ===== NEW: flags & basis ===== */
    @Column(name = "is_liquid_formula", nullable = false)
    private Boolean isLiquidFormula = Boolean.FALSE;   // default false

    @Column(name = "basis_value", precision = 12, scale = 3)
    private BigDecimal basisValue;                     // default 1000

    @Column(name = "basis_unit", length = 10)
    private String basisUnit;                          // default "g"  | "kg" | "l"

    @Column(name = "density", precision = 12, scale = 6)
    private BigDecimal density;                        // g/mL, optional nếu basisUnit="l"

    @OneToMany(
            mappedBy = "formula",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    private List<ProductFormulaItem> formulaItems = new ArrayList<>();
}
