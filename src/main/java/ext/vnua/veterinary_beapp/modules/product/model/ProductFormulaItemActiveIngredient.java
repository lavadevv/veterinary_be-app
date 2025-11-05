package ext.vnua.veterinary_beapp.modules.product.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Tracking của các hoạt chất (Active Ingredients) trong mỗi nguyên vật liệu của công thức.
 * 
 * NGHIỆP VỤ:
 * - Mỗi Material có thể có nhiều MaterialActiveIngredient (hoạt chất)
 * - Khi thêm Material vào công thức, cần theo dõi TỪNG hoạt chất của Material đó
 * - User nhập labelAmount cho từng hoạt chất
 * - Hệ thống tính toán: formulaContentAmount = contentValue × gramsOfMaterial
 * - Hệ thống tính toán: achievedPercent = (formulaContentAmount / labelAmount) × 100
 * 
 * VÍ DỤ:
 * Material: "Amoxicillin Trihydrate" (30.5% trong công thức, tức 305g trên 1000g)
 *   → Active Ingredient 1: Amoxicillin (875 mg/g)
 *      - contentValue: 875 mg/g (từ MaterialActiveIngredient)
 *      - labelAmount: 500 mg (user nhập - nhãn ghi)
 *      - formulaContentAmount: 875 × 305 = 266,875 mg
 *      - achievedPercent: (266,875 / 500) × 100 = 53,375%
 *   → Active Ingredient 2: Clavulanic Acid (125 mg/g)
 *      - contentValue: 125 mg/g
 *      - labelAmount: 125 mg
 *      - formulaContentAmount: 125 × 305 = 38,125 mg
 *      - achievedPercent: (38,125 / 125) × 100 = 30,500%
 */
@Entity
@Table(name = "product_formula_item_active_ingredients", indexes = {
        @Index(name = "idx_pfiai_formula_item", columnList = "formula_item_id"),
        @Index(name = "idx_pfiai_active_ingredient", columnList = "active_ingredient_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFormulaItemActiveIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_item_id", nullable = false)
    private ProductFormulaItem formulaItem;

    /**
     * Reference đến MaterialActiveIngredient.activeIngredient
     * Nullable: cho phép trường hợp material chưa có active ingredient
     */
    @Column(name = "active_ingredient_id")
    private Long activeIngredientId;

    /**
     * SNAPSHOT: Lưu tên hoạt chất tại thời điểm tạo công thức
     * (tránh phụ thuộc vào master data thay đổi sau này)
     */
    @Column(name = "active_ingredient_name", length = 255)
    private String activeIngredientName;

    /**
     * SNAPSHOT: Hàm lượng hoạt chất trong nguyên vật liệu
     * Copied from MaterialActiveIngredient.contentValue
     * Ví dụ: 875 (mg/g), 80 (%), 100000 (IU/g)
     */
    @Column(name = "content_value", precision = 18, scale = 6)
    private BigDecimal contentValue;

    /**
     * SNAPSHOT: Đơn vị hàm lượng
     * Copied from MaterialActiveIngredient.contentUnit
     * Ví dụ: "mg/g", "%", "IU/g"
     */
    @Column(name = "content_unit", length = 30)
    private String contentUnit;

    /**
     * USER INPUT: Hàm lượng ghi trên nhãn (label)
     * Ví dụ: 500 (mg), 125 (mg)
     */
    @Column(name = "label_amount", precision = 18, scale = 6)
    private BigDecimal labelAmount;

    /**
     * USER INPUT: Đơn vị hàm lượng nhãn
     * Ví dụ: "mg", "g", "kg", "IU"
     */
    @Column(name = "label_unit", length = 20)
    private String labelUnit;

    /**
     * CALCULATED: Hàm lượng hoạt chất đạt được trong công thức
     * = contentValue × gramsOfMaterial (từ percentage hoặc quantity của parent item)
     * Ví dụ: 875 mg/g × 305g = 266,875 mg
     */
    @Column(name = "formula_content_amount", precision = 18, scale = 6)
    private BigDecimal formulaContentAmount;

    /**
     * CALCULATED: Đơn vị hàm lượng công thức
     * Thường là "mg" sau khi quy đổi
     */
    @Column(name = "formula_content_unit", length = 20)
    private String formulaContentUnit;

    /**
     * CALCULATED: Phần trăm đạt được so với nhãn
     * = (formulaContentAmount / labelAmount) × 100
     * Ví dụ: (266,875 / 500) × 100 = 53,375%
     */
    @Column(name = "achieved_percent", precision = 12, scale = 6)
    private BigDecimal achievedPercent;

    /**
     * Ghi chú riêng cho hoạt chất này
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
