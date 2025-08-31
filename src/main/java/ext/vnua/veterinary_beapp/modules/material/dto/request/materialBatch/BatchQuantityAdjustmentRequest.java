package ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BatchQuantityAdjustmentRequest {
    @NotNull(message = "ID lô không được để trống")
    private Long batchId;

    @NotNull(message = "Số lượng điều chỉnh không được để trống")
    @DecimalMin(value = "-999999999.999", message = "Số lượng điều chỉnh không hợp lệ")
    private BigDecimal adjustmentQuantity; // Can be negative for consumption

    @NotNull(message = "Lý do điều chỉnh không được để trống")
    private String reason;

    private String notes;
}