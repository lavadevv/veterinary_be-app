package ext.vnua.veterinary_beapp.modules.material.dto.request.materialCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMaterialFormTypeRequest {
    @NotBlank(message = "Tên dạng vật liệu không được để trống")
    @Size(max = 100, message = "Tên không vượt quá 100 ký tự")
    private String name;
}
