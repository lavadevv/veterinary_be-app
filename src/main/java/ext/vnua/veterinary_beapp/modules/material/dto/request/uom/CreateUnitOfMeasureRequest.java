package ext.vnua.veterinary_beapp.modules.material.dto.request.uom;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUnitOfMeasureRequest {
    @NotBlank(message = "Tên đơn vị không được trống")
    private String name;
}
