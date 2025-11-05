// File: ext/vnua/veterinary_beapp/modules/material/dto/request/material/CreateMaterialRequest.java
package ext.vnua.veterinary_beapp.modules.material.dto.request.material;

import ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.MaterialActiveIngredientRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateMaterialRequest {

    @NotBlank(message = "Mã vật liệu không được để trống")
    @Size(max = 50, message = "Mã vật liệu không được vượt quá 50 ký tự")
    private String materialCode;

    @NotBlank(message = "Tên vật liệu không được để trống")
    @Size(max = 255, message = "Tên vật liệu không được vượt quá 255 ký tự")
    private String materialName;

    @Size(max = 150, message = "Tên quốc tế không được vượt quá 150 ký tự")
    private String internationalName;

    @NotNull(message = "Loại vật liệu (category) không được để trống")
    @Positive(message = "ID loại vật liệu phải > 0")
    private Long materialCategoryId;

    @Positive(message = "ID dạng vật liệu phải > 0")
    private Long materialFormTypeId;

    @Valid
    private List<MaterialActiveIngredientRequest> activeIngredients;

    @DecimalMin(value = "0.0", message = "Độ tinh khiết không được âm")
    @DecimalMax(value = "100.0", message = "Độ tinh khiết không được vượt quá 100%")
    private BigDecimal purityPercentage;

    @DecimalMin(value = "0.0", message = "IU/gram không được âm")
    private BigDecimal iuPerGram;

    @Size(max = 50, message = "Màu sắc không được vượt quá 50 ký tự")
    private String color;

    @Size(max = 100, message = "Mùi không được vượt quá 100 ký tự")
    private String odor;

    @DecimalMin(value = "0.0", message = "Độ ẩm không được âm")
    @DecimalMax(value = "100.0", message = "Độ ẩm không được vượt quá 100%")
    private BigDecimal moistureContent;

    @DecimalMin(value = "0.0", message = "Độ nhớt không được âm")
    private BigDecimal viscosity;

    @NotNull(message = "Đơn vị đo không được để trống")
    @Positive(message = "ID đơn vị đo phải > 0")
    private Long unitOfMeasureId;

    @Size(max = 500, message = "Tiêu chuẩn áp dụng không được vượt quá 500 ký tự")
    private String standardApplied;

    @Positive(message = "ID nhà cung cấp phải > 0")
    private Long supplierId;

    @DecimalMin(value = "0.0", message = "Mức tồn kho tối thiểu không được âm")
    private BigDecimal minimumStockLevel;

    @DecimalMin(value = "0.0", message = "Giá cố định không được âm")
    private BigDecimal fixedPrice;

    private Boolean requiresColdStorage;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean isActive;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;
}
