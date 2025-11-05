package ext.vnua.veterinary_beapp.modules.product.model;

import ext.vnua.veterinary_beapp.modules.material.model.Material;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_formula_items", indexes = {
        @Index(name = "idx_formula_item_formula", columnList = "formula_id"),
        @Index(name = "idx_formula_item_material", columnList = "material_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFormulaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_id", nullable = false)
    private ProductFormula formula;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "quantity", precision = 12, scale = 6)
    private BigDecimal quantity; // Định mức nguyên liệu (nếu dùng)

    @Column(name = "unit")
    private String unit;

    @Column(name = "percentage", precision = 8, scale = 4)
    private BigDecimal percentage; // Tỷ lệ %

    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /* ===== NEW: label & calculated fields ===== */
    @Column(name = "label_amount", precision = 18, scale = 6)
    private BigDecimal labelAmount;              // Nhập từ người dùng (optional)

    @Column(name = "label_unit", length = 20)
    private String labelUnit;

    @Column(name = "formula_content_amount", precision = 18, scale = 6)
    private BigDecimal formulaContentAmount;     // mg (đã quy đổi)

    @Column(name = "formula_content_unit", length = 20)
    private String formulaContentUnit;           // luôn "mg" trong Hướng B

    @Column(name = "achieved_percent", precision = 7, scale = 1)
    private BigDecimal achievedPercent;          // % đạt (làm tròn 1 số)

    /**
     * NEW: Danh sách các hoạt chất (Active Ingredients) tracking
     * Mỗi Material có thể có nhiều hoạt chất, cần theo dõi riêng từng hoạt chất
     * User sẽ nhập labelAmount cho từng hoạt chất và hệ thống tính toán % đạt
     */
    @OneToMany(
            mappedBy = "formulaItem",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    private List<ProductFormulaItemActiveIngredient> activeIngredients = new ArrayList<>();
}
