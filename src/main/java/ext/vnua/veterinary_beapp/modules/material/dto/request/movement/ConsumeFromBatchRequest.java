package ext.vnua.veterinary_beapp.modules.material.dto.request.movement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConsumeFromBatchRequest {

    @NotNull
    private Long batchId;

    @NotNull
    @DecimalMin(value = "0.001", message = "Số lượng phải > 0")
    private BigDecimal quantity;

    private String note;
}
