package ext.vnua.veterinary_beapp.modules.product.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.product.enums.FormulationType;
import ext.vnua.veterinary_beapp.modules.product.enums.ProductCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_code", columnList = "product_code", unique = true),
        @Index(name = "idx_product_category", columnList = "product_category"),
        @Index(name = "idx_product_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", unique = true, nullable = false)
    private String productCode; // TCCS 01/2021 DYVN-phospho ca+ daeyong

    @Column(name = "product_name", nullable = false)
    private String productName; // Amoxcoli 500 -- bột -- 1kg (10/1*) - Daeyong

    @Column(name = "short_name")
    private String shortName; // Dùng cho tem nhãn, in mã QR

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category", nullable = false)
    private ProductCategory productCategory; // THANH_PHAM, BAN_THANH_PHAM

    @Enumerated(EnumType.STRING)
    @Column(name = "formulation_type", nullable = false)
    private FormulationType formulationType; // Bột uống, dung dịch, viên, tiêm, hỗn dịch, gel...

    @Column(name = "packaging_specification", nullable = false)
    private String packagingSpecification; // 100g/hũ, 10ml/ống x 10 ống/hộp, 25kg/bao...

    @Column(name = "brand_name")
    private String brandName; // Daeyong, Hope, Oringer...

    @Column(name = "quality_standard", columnDefinition = "TEXT")
    private String qualityStandard; // Dược điển (nếu có), TCCS, hoặc theo quy định nội bộ

    @Column(name = "registration_number")
    private String registrationNumber; // Số hồ sơ đăng ký hoặc công bố sản phẩm

    @Column(name = "circulation_code")
    private String circulationCode; // Mã số công bố hoặc số lưu hành

    @Column(name = "shelf_life_months")
    private Integer shelfLifeMonths; // Thời hạn sử dụng tính bằng tháng

    @Column(name = "unit_of_measure", nullable = false)
    private String unitOfMeasure; // Đơn vị tính: hộp, lọ, kg, túi...

    @Column(name = "current_stock")
    private Double currentStock = 0.0;

    @Column(name = "minimum_stock_level")
    private Double minimumStockLevel;

    @Column(name = "cost_price")
    private Double costPrice; // Giá cost = giá nguyên liệu + chi phí sản xuất

    @Column(name = "profit_margin_percentage")
    private Double profitMarginPercentage; // Tỷ lệ % lợi nhuận trước thuế

    @Column(name = "selling_price")
    private Double sellingPrice; // Giá bán = cost price * (1 + profit margin)

    @Column(name = "requires_cold_storage", nullable = false)
    private Boolean requiresColdStorage = false;

    @Column(name = "special_storage_conditions", columnDefinition = "TEXT")
    private String specialStorageConditions;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductFormula> formulas;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductBatch> batches;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProductRegistration registration;
}
