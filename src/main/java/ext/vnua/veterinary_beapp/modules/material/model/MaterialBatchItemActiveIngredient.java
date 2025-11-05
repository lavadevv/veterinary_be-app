package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Hàm lượng hoạt chất của một MaterialBatchItem cụ thể
 * Thay thế cho MaterialBatchActiveIngredient trong logic mới
 */
@Entity
@Table(name = "material_batch_item_active_ingredients", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_batch_item_active_ingredient", 
                           columnNames = {"batch_item_id", "active_ingredient_id"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MaterialBatchItemActiveIngredient extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** MaterialBatchItem mà hoạt chất này thuộc về */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_item_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_batch_item_ai_batch_item"))
    private MaterialBatchItem batchItem;

    /** Hoạt chất */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_ingredient_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_batch_item_ai_active_ingredient"))
    private ActiveIngredient activeIngredient;

    // ===== COA (Certificate of Analysis) - Hàm lượng theo chứng nhận =====
    @Column(name = "coa_content_value", precision = 18, scale = 6)
    private BigDecimal coaContentValue;

    @Column(name = "coa_content_unit", length = 50)
    private String coaContentUnit;

    @Column(name = "coa_min_value", precision = 18, scale = 6)
    private BigDecimal coaMinValue;

    @Column(name = "coa_max_value", precision = 18, scale = 6)
    private BigDecimal coaMaxValue;

    @Column(name = "coa_notes", columnDefinition = "TEXT")
    private String coaNotes;

    // ===== KQPT (Kết quả phân tích thực tế) =====
    @Column(name = "test_content_value", precision = 18, scale = 6)
    private BigDecimal testContentValue;

    @Column(name = "test_content_unit", length = 50)
    private String testContentUnit;

    @Column(name = "test_date")
    private java.time.LocalDate testDate;

    @Column(name = "test_method", length = 255)
    private String testMethod;

    @Column(name = "test_notes", columnDefinition = "TEXT")
    private String testNotes;

    /**
     * Kiểm tra hoạt chất có đạt chuẩn không
     * Đạt chuẩn: Tỷ lệ KQPT/COA từ 90% đến 110%
     * @return true nếu đạt, false nếu không đạt, null nếu chưa có dữ liệu để so sánh
     */
    public Boolean isQualified() {
        if (testContentValue == null || coaContentValue == null) {
            return null; // Chưa có đủ dữ liệu để đánh giá
        }

        if (coaContentValue.signum() == 0) {
            return null; // COA = 0, không thể tính tỷ lệ
        }

        // Tính tỷ lệ KQPT/COA * 100%
        BigDecimal ratio = testContentValue
                .divide(coaContentValue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        // Đạt chuẩn: 90% <= ratio <= 110%
        return ratio.compareTo(new BigDecimal("90")) >= 0 
                && ratio.compareTo(new BigDecimal("110")) <= 0;
    }

    /**
     * Tính tỷ lệ KQPT/COA theo phần trăm
     * @return Tỷ lệ phần trăm, null nếu không có đủ dữ liệu
     */
    public BigDecimal getRatioPercentage() {
        if (testContentValue == null || coaContentValue == null || coaContentValue.signum() == 0) {
            return null;
        }

        return testContentValue
                .divide(coaContentValue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * Tính độ lệch % so với giá trị COA
     * @return % lệch so với COA, null nếu không có đủ dữ liệu
     */
    public BigDecimal getDeviationPercentage() {
        if (testContentValue == null || coaContentValue == null || coaContentValue.signum() == 0) {
            return null;
        }

        BigDecimal deviation = testContentValue.subtract(coaContentValue);
        return deviation.divide(coaContentValue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hoạt chất: ").append(activeIngredient != null ? activeIngredient.getIngredientName() : "N/A").append("\n");

        if (coaContentValue != null) {
            sb.append("  COA: ").append(coaContentValue.stripTrailingZeros().toPlainString());
            if (coaContentUnit != null) sb.append(" ").append(coaContentUnit);
            sb.append("\n");
        }

        if (testContentValue != null) {
            sb.append("  KQPT: ").append(testContentValue.stripTrailingZeros().toPlainString());
            if (testContentUnit != null) sb.append(" ").append(testContentUnit);
            
            BigDecimal ratio = getRatioPercentage();
            if (ratio != null) {
                sb.append(" (Tỷ lệ: ").append(ratio.stripTrailingZeros().toPlainString()).append("%)");
            }
            
            sb.append(" - Trạng thái: ").append(getQualificationStatus());
            sb.append("\n");
        }

        return sb.toString();
    }

    private String getQualificationStatus() {
        Boolean qualified = isQualified();
        if (qualified == null) return "Chưa có dữ liệu";
        return qualified ? "Đạt (90-110%)" : "Không đạt";
    }
}
