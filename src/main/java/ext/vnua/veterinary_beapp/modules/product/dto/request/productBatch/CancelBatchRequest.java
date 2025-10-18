package ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelBatchRequest {
    @NotNull
    private Long batchId;
    private String reason; // tùy chọn
}