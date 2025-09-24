package ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PlanBatchRequest {
    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;

    @NotNull(message = "Số lượng kế hoạch không được để trống")
    @Min(value = 1, message = "Số lượng kế hoạch phải > 0")
    private BigDecimal plannedQuantity; // số lượng thành phẩm dự kiến (đơn vị tính theo Product.unitOfMeasure)

    @NotNull(message = "Ngày sản xuất không được để trống")
    private LocalDate manufacturingDate;

    // Optional: công thức; nếu null → chọn công thức active mới nhất
    private Long formulaId;
}