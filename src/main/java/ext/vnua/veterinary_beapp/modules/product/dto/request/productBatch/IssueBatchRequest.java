package ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class IssueBatchRequest {
    @NotNull private Long productId;
    @NotNull private BigDecimal plannedQuantity;
    @NotNull private LocalDate manufacturingDate;
    private Long formulaId; // optional

    // Cho phép chọn lô NVL khác FIFO
    private boolean allowAlternativeBatches = true;

    // Nếu người dùng override FIFO: danh sách pick NVL (materialBatchId, quantity)
    @Valid
    private List<MaterialPickRequest> picks;

    @Data
    public static class MaterialPickRequest {
        @NotNull private Long materialBatchId;
        @NotNull private BigDecimal quantity;
    }
}
