package ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateActiveIngredientRequest {
    
    @NotBlank(message = "Mã hoạt chất không được để trống")
    @Size(max = 50, message = "Mã hoạt chất không được vượt quá 50 ký tự")
    private String ingredientCode;
    
    @NotBlank(message = "Tên hoạt chất không được để trống")
    @Size(max = 255, message = "Tên hoạt chất không được vượt quá 255 ký tự")
    private String ingredientName;
    
    @Size(max = 50, message = "Số CAS không được vượt quá 50 ký tự")
    private String casNumber;
    
    private String description;
    
    private Boolean isActive = true;
}