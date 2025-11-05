package ext.vnua.veterinary_beapp.modules.material.dto.request.warehouseType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWarehouseTypeRequest {
    @NotNull(message = "Id không được để trống")
    private Long id;

    private String name;
}