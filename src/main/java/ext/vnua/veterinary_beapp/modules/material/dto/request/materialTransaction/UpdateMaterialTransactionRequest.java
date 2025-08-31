package ext.vnua.veterinary_beapp.modules.material.dto.request.materialTransaction;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialTransaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateMaterialTransactionRequest {

    @NotNull(message = "ID không được để trống")
    @Min(value = 1, message = "ID phải lớn hơn 0")
    private Long id;

    @NotNull(message = "ID batch vật liệu không được để trống")
    @Min(value = 1, message = "ID batch vật liệu phải lớn hơn 0")
    private Long materialBatchId;

    @NotNull(message = "Loại giao dịch không được để trống")
    private MaterialTransaction.TransactionType transactionType;

    @NotNull(message = "Thời gian giao dịch không được để trống")
    private LocalDateTime transactionDate;

    @NotNull(message = "Số lượng không được để trống")
    @DecimalMin(value = "0.001", message = "Số lượng phải lớn hơn 0")
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", message = "Đơn giá không được âm")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Tổng giá trị không được âm")
    private BigDecimal totalValue;

    @Size(max = 100, message = "Số chứng từ không được vượt quá 100 ký tự")
    private String referenceDocument;

    @Size(max = 50, message = "Mã đơn sản xuất không được vượt quá 50 ký tự")
    private String productionOrderId;

    private Long fromLocationId;

    private Long toLocationId;

    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự")
    private String reason;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;

    private Long approvedById;
}
