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
    private String version; // v1.0, v1.1, v2.0...

    @Column(name = "batch_size", nullable = false, precision = 10, scale = 3)
    private BigDecimal batchSize; // Kích thước lô sản xuất chuẩn

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "sop_file_path")
    private String sopFilePath; // Link đến file SOP hoặc TCCS

    // Relationships
//    @OneToMany(mappedBy = "formula", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<ProductFormulaItem> formulaItems = new ArrayList<>();

    @OneToMany(
            mappedBy = "formula",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    private List<ProductFormulaItem> formulaItems = new ArrayList<>();

}
