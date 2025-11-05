package ext.vnua.veterinary_beapp.modules.product.dto.request.product;

import ext.vnua.veterinary_beapp.modules.product.enums.FormulationType;
import ext.vnua.veterinary_beapp.modules.product.enums.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Mã sản phẩm không được để trống")
    private String productCode;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;

    private String shortName;

    @NotNull(message = "Danh mục sản phẩm không được để trống")
    private ProductCategory productCategory;

    @NotNull(message = "Dạng bào chế không được để trống")
    private FormulationType formulationType;

    // ❌ REMOVED: packagingSpecification - Moved to ProductBrand
    // ❌ REMOVED: brandName - Moved to ProductBrand
    // ❌ REMOVED: qualityStandard, registrationNumber, circulationCode - Moved to ProductBrand

    @Min(value = 1, message = "Hạn sử dụng phải lớn hơn 0 tháng")
    private Integer shelfLifeMonths;

    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unitOfMeasure;

    // READ-ONLY: currentStock is calculated from inventory transactions
    // Should not be set by user, but can be included for backward compatibility
    @DecimalMin(value = "0.0", message = "Tồn kho hiện tại không được âm")
    private Double currentStock = 0.0;

    private Double minimumStockLevel;
    
    // ❌ REMOVED: costPrice, profitMarginPercentage, sellingPrice - Moved to ProductBrand
    
    private Boolean requiresColdStorage = false;
    private String specialStorageConditions;
    private Boolean isActive = true;
    private String notes;
}
