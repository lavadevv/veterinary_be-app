package ext.vnua.veterinary_beapp.modules.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho ProductBrand entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBrandDto {
    
    private Long id;
    
    // Product info
    private Long productId;
    private String productCode;
    private String productName;
    
    // Brand info
    private Long brandId;
    private String brandName;
    
    // Production Cost Sheet info
    private Long productionCostSheetId;
    private String productionCostSheetCode;
    private String productionCostSheetName;
    
    // Packaging & Specification
    private String packagingSpecification;
    
    // Registration info
    private String registrationNumber;
    private String circulationCode;
    private String qualityStandard;
    
    // Cost & Pricing
    private BigDecimal materialCost;
    private BigDecimal productionUnitCost;
    private BigDecimal profitMarginPercentage;
    private BigDecimal vatPercentage;
    private BigDecimal sellingPrice; // Auto-calculated
    
    // Status
    private Boolean isActive;
    private String notes;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
