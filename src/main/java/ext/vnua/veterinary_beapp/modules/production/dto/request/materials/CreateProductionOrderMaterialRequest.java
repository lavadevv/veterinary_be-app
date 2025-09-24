package ext.vnua.veterinary_beapp.modules.production.dto.request.materials;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductionOrderMaterialRequest {
    @NotNull
    private Long productionOrderId;

    @NotNull
    private Long materialBatchId;

    @NotNull
    private BigDecimal requiredQuantity;

    private String notes;
}
