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

    @NotBlank(message = "Quy cách đóng gói không được để trống")
    private String packagingSpecification;

    private String brandName;
    private String qualityStandard;
    private String registrationNumber;
    private String circulationCode;
    private Integer shelfLifeMonths;

    @NotBlank(message = "Đơn vị tính không được để trống")
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
