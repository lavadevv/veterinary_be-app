package ext.vnua.veterinary_beapp.modules.product.dto.request.product;

import ext.vnua.veterinary_beapp.modules.product.enums.FormulationType;
import ext.vnua.veterinary_beapp.modules.product.enums.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProductRequest {

    @NotNull(message = "ID sản phẩm không được để trống")
    private Long id;

    @NotBlank(message = "Mã sản phẩm không được để trống")
    private String productCode;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;

    private String shortName;

    @NotNull(message = "Danh mục sản phẩm không được để trống")
    private ProductCategory productCategory;

    @NotNull(message = "Dạng bào chế không được để trống")
    private FormulationType formulationType;

    // ❌ REMOVED: packagingSpecification, brandName - Moved to ProductBrand
    // ❌ REMOVED: qualityStandard, registrationNumber, circulationCode - Moved to ProductBrand
    
    private Integer shelfLifeMonths;

    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unitOfMeasure;

    // READ-ONLY: currentStock should not be updated directly
    // Use inventory transaction APIs instead
    private Double currentStock;
    
    private Double minimumStockLevel;
    
    // ❌ REMOVED: costPrice, profitMarginPercentage, sellingPrice - Moved to ProductBrand
    
    private Boolean requiresColdStorage;
    private String specialStorageConditions;
    private Boolean isActive;
    private String notes;
}
