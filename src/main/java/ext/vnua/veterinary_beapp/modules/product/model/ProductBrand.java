package ext.vnua.veterinary_beapp.modules.product.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.model.Brand;
import ext.vnua.veterinary_beapp.modules.pcost.model.ProductionCostSheet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Bảng trung gian Product - Brand
 * Lưu thông tin sản phẩm theo từng thương hiệu khác nhau
 * Ví dụ: Lactoc-Daeyong, Lactoc-Yowin có chi phí sản xuất và giá bán khác nhau
 */
@Entity
@Table(name = "product_brands", 
    indexes = {
        @Index(name = "idx_product_brand", columnList = "product_id, brand_id", unique = true),
        @Index(name = "idx_pb_product", columnList = "product_id"),
        @Index(name = "idx_pb_brand", columnList = "brand_id"),
        @Index(name = "idx_pb_active", columnList = "is_active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_brand", columnNames = {"product_id", "brand_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductBrand extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== RELATIONSHIPS =====
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_cost_sheet_id")
    private ProductionCostSheet productionCostSheet;

    // ===== PACKAGING & SPECIFICATION =====
    
    /**
     * Quy cách đóng gói (VD: "1/1", "10/1", "100g/hũ")
     */
    @Column(name = "packaging_specification", length = 300)
    private String packagingSpecification;

    // ===== REGISTRATION INFO =====
    
    @Column(name = "registration_number", length = 150)
    private String registrationNumber; // Số hồ sơ đăng ký

    @Column(name = "circulation_code", length = 150)
    private String circulationCode; // Mã số công bố/lưu hành

    @Column(name = "quality_standard", columnDefinition = "TEXT")
    private String qualityStandard; // TCCS, Dược điển

    // ===== COST & PRICING =====
    
    /**
     * Chi phí nguyên liệu (tính từ Formula)
     * Được cập nhật từ ProductFormula calculation
     */
    @Column(name = "material_cost", precision = 18, scale = 2)
    private BigDecimal materialCost = BigDecimal.ZERO;

    /**
     * Chi phí sản xuất đơn vị (lấy từ ProductionCostSheet.unitCost)
     * Mỗi brand có thể có chi phí sản xuất khác nhau
     */
    @Column(name = "production_unit_cost", precision = 18, scale = 2)
    private BigDecimal productionUnitCost = BigDecimal.ZERO;

    /**
     * Tỷ lệ lợi nhuận (0-100)
     * Người dùng nhập, có thể khác nhau cho mỗi brand
     */
    @Column(name = "profit_margin_percentage", precision = 5, scale = 2)
    private BigDecimal profitMarginPercentage = BigDecimal.ZERO;

    /**
     * Tỷ lệ VAT (0-100)
     * Người dùng nhập, có thể khác nhau cho mỗi brand
     */
    @Column(name = "vat_percentage", precision = 5, scale = 2)
    private BigDecimal vatPercentage = BigDecimal.ZERO;

    /**
     * Giá bán (TỰ ĐỘNG TÍNH)
     * Formula: (materialCost + productionUnitCost) × (1 + profitMargin/100) × (1 + vat/100)
     */
    @Column(name = "selling_price", precision = 18, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    // ===== STATUS =====
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ===== AUTO-CALCULATION =====

    /**
     * Tự động tính giá bán trước khi lưu/cập nhật
     * Formula: sellingPrice = (materialCost + productionUnitCost) × (1 + profitMargin/100) × (1 + vat/100)
     */
    @PrePersist
    @PreUpdate
    public void calculateSellingPrice() {
        // Ensure non-null values
        if (materialCost == null) materialCost = BigDecimal.ZERO;
        if (productionUnitCost == null) productionUnitCost = BigDecimal.ZERO;
        if (profitMarginPercentage == null) profitMarginPercentage = BigDecimal.ZERO;
        if (vatPercentage == null) vatPercentage = BigDecimal.ZERO;

        // Calculate base cost
        BigDecimal baseCost = materialCost.add(productionUnitCost);

        // Apply profit margin: baseCost × (1 + profitMargin/100)
        BigDecimal profitMultiplier = BigDecimal.ONE.add(
            profitMarginPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        BigDecimal costWithProfit = baseCost.multiply(profitMultiplier);

        // Apply VAT: costWithProfit × (1 + vat/100)
        BigDecimal vatMultiplier = BigDecimal.ONE.add(
            vatPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        
        sellingPrice = costWithProfit.multiply(vatMultiplier)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Helper method: Cập nhật chi phí sản xuất từ ProductionCostSheet
     */
    public void updateProductionCostFromSheet() {
        if (this.productionCostSheet != null && this.productionCostSheet.getUnitCost() != null) {
            this.productionUnitCost = this.productionCostSheet.getUnitCost();
        }
    }
}
