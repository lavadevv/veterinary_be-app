package ext.vnua.veterinary_beapp.modules.product.model;

import ext.vnua.veterinary_beapp.modules.material.model.Material;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    @Column(name = "quantity", nullable = false, precision = 12, scale = 6)
    private BigDecimal quantity; // Định mức nguyên liệu

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "percentage", precision = 8, scale = 4)
    private BigDecimal percentage; // Tỷ lệ %

    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical = false; // Nguyên liệu quan trọng cần kiểm soát đặc biệt

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}