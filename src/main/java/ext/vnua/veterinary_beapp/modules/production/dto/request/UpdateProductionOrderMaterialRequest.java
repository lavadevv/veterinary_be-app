package ext.vnua.veterinary_beapp.modules.production.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductionOrderMaterialRequest {
    @NotNull
    private Long id; // id của ProductionOrderMaterial

    private BigDecimal issuedQuantity;
    private BigDecimal actualQuantity;
    private String status;
    private String notes;
}
