package ext.vnua.veterinary_beapp.modules.material.dto.request.uom;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUnitOfMeasureRequest {
    @NotNull(message = "ID không được trống")
    private Long id;

    private String name; // optional
}