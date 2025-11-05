package ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaterialActiveIngredientRequest {
    
    @NotNull(message = "ID hoạt chất không được để trống")
    private Long activeIngredientId;
    
    @DecimalMin(value = "0.0", message = "Hàm lượng phải lớn hơn hoặc bằng 0")
    private BigDecimal contentValue;
    
    private String contentUnit; // "%", "IU/g", "mg/g", v.v.
    
    private String notes;
}