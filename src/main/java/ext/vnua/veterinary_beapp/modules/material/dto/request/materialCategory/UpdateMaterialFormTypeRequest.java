package ext.vnua.veterinary_beapp.modules.material.dto.request.materialCategory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMaterialFormTypeRequest {
    @NotNull(message = "ID không được để trống")
    private Long id;

    @Size(max = 100, message = "Tên không vượt quá 100 ký tự")
    private String name; // optional
}