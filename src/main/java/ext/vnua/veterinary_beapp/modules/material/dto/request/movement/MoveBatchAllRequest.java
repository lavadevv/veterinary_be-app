package ext.vnua.veterinary_beapp.modules.material.dto.request.movement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveBatchAllRequest {

    @NotNull
    private Long batchId;

    @NotNull
    private Long toLocationId;

    private String note;
}
