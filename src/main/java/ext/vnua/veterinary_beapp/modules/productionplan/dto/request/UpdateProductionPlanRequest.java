package ext.vnua.veterinary_beapp.modules.productionplan.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateProductionPlanRequest {

    @NotNull
    private Long formulaId;

    @NotNull
    @DecimalMin(value = "0.001", message = "Batch size must be greater than 0")
    private BigDecimal batchSize;

    private LocalDate planDate;

    private String notes;

    @Valid
    @NotEmpty(message = "Products to produce must not be empty")
    private List<CreateProductionPlanRequest.ProductLine> products;
}
