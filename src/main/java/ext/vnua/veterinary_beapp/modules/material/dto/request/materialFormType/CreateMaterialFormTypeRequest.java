// File: ext/vnua/veterinary_beapp/modules/material/dto/request/materialFormType/CreateMaterialFormTypeRequest.java
package ext.vnua.veterinary_beapp.modules.material.dto.request.materialFormType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMaterialFormTypeRequest {
    @NotBlank(message = "Tên dạng vật liệu không được để trống")
    @Size(max = 100, message = "Tên tối đa 100 ký tự")
    private String name;  // "Bột", "Lỏng", "Hạt", ...
}
