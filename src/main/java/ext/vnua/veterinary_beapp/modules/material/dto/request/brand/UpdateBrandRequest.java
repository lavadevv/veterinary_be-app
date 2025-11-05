package ext.vnua.veterinary_beapp.modules.material.dto.request.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBrandRequest {
    @NotNull(message = "ID không được để trống")
    private Long id;

    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 255, message = "Tên thương hiệu không được vượt quá 255 ký tự")
    private String name;
}
