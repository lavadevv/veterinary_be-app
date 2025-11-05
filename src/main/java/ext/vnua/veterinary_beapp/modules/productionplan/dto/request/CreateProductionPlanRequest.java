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
public class CreateProductionPlanRequest {

    @NotNull
    private Long formulaId;

    @NotNull
    @DecimalMin(value = "0.001", message = "Batch size must be greater than 0")
    private BigDecimal batchSize;

    private LocalDate planDate;

    private String notes;

    @Valid
    @NotEmpty(message = "Products to produce must not be empty")
    private List<ProductLine> products;

    @Data
    public static class ProductLine {
        @NotNull
        private Long productId;

        private String productBrand;

        @NotNull
        @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
        private BigDecimal plannedQuantity;

        /** Actual quantity entered by the operator. */
        private BigDecimal actualQuantity;

        private Long productionCostSheetId;

        /**
         * Allow overriding the unit cost manually. If null, the sheet unit cost will be used.
         */
        @DecimalMin(value = "0.0", inclusive = false, message = "Unit cost must be greater than 0")
        private BigDecimal plannedUnitCost;

        private String notes;
    }
}
