package ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CalcBatchReq(
        @NotNull Long formulaId,
        @NotNull @Min(1) BigDecimal batchSizeKg
) {}