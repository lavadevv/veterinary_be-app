package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.enums.MaterialForm;
import ext.vnua.veterinary_beapp.modules.material.enums.MaterialType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Material extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_code", unique = true, nullable = false, length = 50)
    private String materialCode;

    @Column(name = "material_name", nullable = false, length = 255)
    private String materialName;

    @Column(name = "short_name", length = 100)
    private String shortName;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, length = 50)
    private MaterialType materialType;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_form", length = 50)
    private MaterialForm materialForm;

    @Column(name = "active_ingredient", columnDefinition = "TEXT")
    private String activeIngredient;

    @Column(name = "purity_percentage", precision = 10, scale = 4)
    private BigDecimal purityPercentage;

    @Column(name = "iu_per_gram", precision = 18, scale = 6)
    private BigDecimal iuPerGram;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "odor", length = 100)
    private String odor;

    @Column(name = "moisture_content", precision = 10, scale = 4)
    private BigDecimal moistureContent;

    @Column(name = "viscosity", precision = 18, scale = 6)
    private BigDecimal viscosity;

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure; // kg, g, ml, L, cái, cuộn, lọ, m2...

    @Column(name = "standard_applied", columnDefinition = "TEXT")
    private String standardApplied; // USP, BP, EP, tiêu chuẩn nhà sản xuất...

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "minimum_stock_level", precision = 18, scale = 6)
    private BigDecimal minimumStockLevel;

    @Column(name = "current_stock", precision = 18, scale = 6, nullable = false)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "fixed_price", precision = 18, scale = 2)
    private BigDecimal fixedPrice; // Đơn giá cố định/giá chuẩn

    @Column(name = "requires_cold_storage", nullable = false)
    private Boolean requiresColdStorage = false;

    @Column(name = "special_handling", columnDefinition = "TEXT")
    private String specialHandling;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Relationships
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaterialBatch> batches;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<MaterialPriceHistory> priceHistories = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Thông tin nguyên liệu ===\n");
        sb.append("Mã nguyên liệu: ").append(materialCode).append("\n");
        sb.append("Tên nguyên liệu: ").append(materialName).append("\n");

        if (shortName != null) {
            sb.append("Tên viết tắt: ").append(shortName).append("\n");
        }

        sb.append("Loại: ").append(materialType != null ? materialType.getDisplayName() : "Chưa xác định").append("\n");
        sb.append("Dạng: ").append(materialForm != null ? materialForm.getDisplayName() : "Chưa xác định").append("\n");

        if (activeIngredient != null) {
            sb.append("Hoạt chất chính: ").append(activeIngredient).append("\n");
        }

        if (purityPercentage != null) {
            sb.append("Độ tinh khiết: ").append(purityPercentage.stripTrailingZeros().toPlainString()).append("%\n");
        }

        if (iuPerGram != null) {
            sb.append("Hàm lượng (IU/gram): ").append(iuPerGram.stripTrailingZeros().toPlainString()).append("\n");
        }

        if (color != null) {
            sb.append("Màu sắc: ").append(color).append("\n");
        }
        if (odor != null) {
            sb.append("Mùi: ").append(odor).append("\n");
        }
        if (moistureContent != null) {
            sb.append("Độ ẩm: ").append(moistureContent.stripTrailingZeros().toPlainString()).append("%\n");
        }
        if (viscosity != null) {
            sb.append("Độ nhớt: ").append(viscosity.stripTrailingZeros().toPlainString()).append("\n");
        }

        sb.append("Đơn vị tính: ").append(unitOfMeasure).append("\n");

        if (standardApplied != null) {
            sb.append("Tiêu chuẩn áp dụng: ").append(standardApplied).append("\n");
        }

        if (supplier != null) {
            sb.append("Nhà cung cấp: ").append(supplier.getSupplierName()).append("\n");
        }

        if (minimumStockLevel != null) {
            sb.append("Mức tồn kho tối thiểu: ")
                    .append(minimumStockLevel.stripTrailingZeros().toPlainString()).append(" ").append(unitOfMeasure).append("\n");
        }

        sb.append("Tồn kho hiện tại: ")
                .append(currentStock != null ? currentStock.stripTrailingZeros().toPlainString() : "0").append(" ").append(unitOfMeasure).append("\n");

        if (fixedPrice != null) {
            sb.append("Đơn giá cố định: ").append(fixedPrice.stripTrailingZeros().toPlainString()).append(" VND\n");
        }

        sb.append("Yêu cầu bảo quản lạnh: ").append(Boolean.TRUE.equals(requiresColdStorage) ? "Có" : "Không").append("\n");

        if (specialHandling != null) {
            sb.append("Hướng dẫn bảo quản đặc biệt: ").append(specialHandling).append("\n");
        }

        sb.append("Đang sử dụng: ").append(Boolean.TRUE.equals(isActive) ? "Có" : "Không").append("\n");

        if (notes != null) {
            sb.append("Ghi chú: ").append(notes).append("\n");
        }

        return sb.toString();
    }
}
