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

    @NotBlank(message = "Quy cách đóng gói không được để trống")
    private String packagingSpecification;

    private String brandName;
    private String qualityStandard;
    private String registrationNumber;
    private String circulationCode;

    @Min(value = 1, message = "Hạn sử dụng phải lớn hơn 0 tháng")
    private Integer shelfLifeMonths;

    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unitOfMeasure;

    @DecimalMin(value = "0.0", message = "Tồn kho hiện tại không được âm")
    private Double currentStock = 0.0;

    private Double minimumStockLevel;
    private Double costPrice;
    private Double profitMarginPercentage;
    private Double sellingPrice;
    private Boolean requiresColdStorage = false;
    private String specialStorageConditions;
    private Boolean isActive = true;
    private String notes;
}
