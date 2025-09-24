package ext.vnua.veterinary_beapp.modules.production.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateProductionOrderRequest {
    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin(value = "0.001", message = "Sản lượng kế hoạch phải > 0")
    private BigDecimal plannedQuantity;

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;

    private Long productionLineId;

    private String notes;
}
