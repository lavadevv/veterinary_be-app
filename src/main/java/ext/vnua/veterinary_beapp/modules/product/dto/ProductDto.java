package ext.vnua.veterinary_beapp.modules.product.dto;

import ext.vnua.veterinary_beapp.modules.product.enums.FormulationType;
import ext.vnua.veterinary_beapp.modules.product.enums.ProductCategory;
import lombok.Data;

@Data
public class ProductDto {
    private Long id;
    private String productCode;
    private String productName;
    private String shortName;
    private ProductCategory productCategory;
    private FormulationType formulationType;
    private String packagingSpecification;
    private String brandName;
    private String qualityStandard;
    private String registrationNumber;
    private String circulationCode;
    private Integer shelfLifeMonths;
    private String unitOfMeasure;
    private Double currentStock;
    private Double minimumStockLevel;
    private Double costPrice;
    private Double profitMarginPercentage;
    private Double sellingPrice;
    private Boolean requiresColdStorage;
    private String specialStorageConditions;
    private Boolean isActive;
    private String notes;
}
