package ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchTransferRequest {
    @NotEmpty(message = "Danh sách ID lô không được để trống")
    private List<Long> batchIds;

    private Long newLocationId; // null means remove from location

    private String reason;
}
