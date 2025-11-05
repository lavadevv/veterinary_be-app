package ext.vnua.veterinary_beapp.modules.material.dto.request.warehouseType;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWarehouseTypeRequest {
    @NotBlank(message = "Tên không được để trống")
    private String name;
}
