// File: ext/vnua/veterinary_beapp/modules/material/dto/request/materialFormType/UpdateMaterialFormTypeRequest.java
package ext.vnua.veterinary_beapp.modules.material.dto.request.materialFormType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMaterialFormTypeRequest {
    @NotNull(message = "Id bắt buộc")
    @Min(value = 1, message = "Id không hợp lệ")
    private Long id;

    @NotBlank(message = "Tên dạng vật liệu không được để trống")
    @Size(max = 100, message = "Tên tối đa 100 ký tự")
    private String name;
}
