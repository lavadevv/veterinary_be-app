package ext.vnua.veterinary_beapp.modules.production.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateProductionOrderRequest {
    @NotNull
    private Long id;

    private BigDecimal plannedQuantity;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private Long productionLineId;
    private String notes;
}
