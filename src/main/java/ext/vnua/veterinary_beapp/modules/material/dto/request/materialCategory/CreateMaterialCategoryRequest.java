package ext.vnua.veterinary_beapp.modules.material.dto.request.materialCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaterialCategoryRequest {
    
    @NotBlank(message = "Tên loại vật liệu không được để trống")
    @Size(min = 2, max = 150, message = "Tên loại vật liệu phải từ 2 đến 150 ký tự")
    private String categoryName;
}