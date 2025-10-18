package ext.vnua.veterinary_beapp.modules.material.dto.request.material;

import ext.vnua.veterinary_beapp.modules.material.enums.MaterialForm;
import ext.vnua.veterinary_beapp.modules.material.enums.MaterialType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMaterialRequest {

    @NotBlank @Size(max = 50)
    private String materialCode;

    @NotBlank @Size(max = 255)
    private String materialName;

    @Size(max = 100)
    private String shortName;

    @NotNull
    private MaterialType materialType;

    private MaterialForm materialForm;

    @Size(max = 1000)
    private String activeIngredient;

    @DecimalMin("0.0") @DecimalMax("100.0")
    private BigDecimal purityPercentage;

    @DecimalMin("0.0")
    private BigDecimal iuPerGram;

    @Size(max = 50)
    private String color;

    @Size(max = 100)
    private String odor;

    @DecimalMin("0.0") @DecimalMax("100.0")
    private BigDecimal moistureContent;

    @DecimalMin("0.0")
    private BigDecimal viscosity;

    @NotBlank @Size(max = 20)
    private String unitOfMeasure;

    @Size(max = 500)
    private String standardApplied;

    @NotNull @Min(1)
    private Long supplierId;

    // NGƯỠNG CẢNH BÁO — giữ ở master
    @DecimalMin("0.0")
    private BigDecimal minimumStockLevel;

    @DecimalMin("0.0")
    private BigDecimal fixedPrice;

    // Cho phép null; entity default = false
    private Boolean requiresColdStorage;

    @Size(max = 1000)
    private String specialHandling;

    @Size(max = 1000)
    private String notes;
}
