package ext.vnua.veterinary_beapp.modules.material.dto.request.cost;

import ext.vnua.veterinary_beapp.modules.material.enums.AllocationMethod;
import ext.vnua.veterinary_beapp.modules.material.enums.OverheadType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateOverheadCostRequest {

    @NotNull(message = "ID là bắt buộc")
    private Long id;

    private OverheadType type;

    private LocalDate costDate;

    /** Nếu null khi update costDate, service sẽ auto = costDate về mùng 1 */
    private LocalDate periodMonth;

    @Size(max = 50)
    private String code;

    @Size(max = 255)
    private String title;

    @Size(max = 50)
    private String unitOfMeasure;

    @DecimalMin(value = "0.000", inclusive = false, message = "Số lượng phải > 0")
    @Digits(integer = 12, fraction = 3)
    private BigDecimal quantity;

    @DecimalMin(value = "0.00", inclusive = false, message = "Đơn giá phải > 0")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal unitPrice;

    /** Cho phép set trực tiếp nếu không thay đổi qty/price */
    @DecimalMin(value = "0.00", message = "Thành tiền phải >= 0")
    @Digits(integer = 15, fraction = 2)
    private BigDecimal amount;

    private AllocationMethod suggestedAllocation;

    @Size(max = 2000)
    private String note;

    @Size(max = 100)
    private String refNo;

    @Size(max = 100)
    private String costCenter;

    private Long productId;
    private Long productBatchId;
}
