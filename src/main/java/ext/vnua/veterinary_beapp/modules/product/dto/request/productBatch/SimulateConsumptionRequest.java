package ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SimulateConsumptionRequest {
    @NotNull
    private Long productId;
    @NotNull
    private BigDecimal plannedQuantity;
    private Long formulaId; // optional
}