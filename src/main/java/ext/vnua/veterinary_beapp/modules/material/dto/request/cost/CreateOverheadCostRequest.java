package ext.vnua.veterinary_beapp.modules.material.dto.request.cost;

import ext.vnua.veterinary_beapp.modules.material.enums.AllocationMethod;
import ext.vnua.veterinary_beapp.modules.material.enums.OverheadType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateOverheadCostRequest {

    @NotNull(message = "Loại chi phí là bắt buộc")
    private OverheadType type;

    /** Ngày phát sinh/ghi nhận. Nếu null → mặc định hôm nay */
    private LocalDate costDate;

    /** Kỳ hạch toán. Nếu null → mặc định = costDate về mùng 1 */
    private LocalDate periodMonth;

    /** Mã chi phí (tuỳ chọn), ví dụ: cvc50, dien, ... */
    @Size(max = 50)
    private String code;

    /** Tiêu đề/diễn giải */
    @NotBlank(message = "Tiêu đề chi phí không được để trống")
    @Size(max = 255)
    private String title;

    /** Đơn vị tính, mặc định 'lần' nếu để trống */
    @Size(max = 50)
    private String unitOfMeasure;

    /** Số lượng. Nếu null → mặc định 1 */
    @DecimalMin(value = "0.000", inclusive = false, message = "Số lượng phải > 0")
    @Digits(integer = 12, fraction = 3)
    private BigDecimal quantity;

    /** Đơn giá. Có thể null (trong trường hợp chỉ nhập thành tiền) */
    @DecimalMin(value = "0.00", inclusive = false, message = "Đơn giá phải > 0")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal unitPrice;

    /**
     * Thành tiền (VNĐ). Chỉ dùng khi thiếu quantity hoặc unitPrice.
     * Nếu có đủ quantity & unitPrice, hệ thống sẽ tự tính và bỏ qua giá trị này.
     */
    @DecimalMin(value = "0.00", message = "Thành tiền phải >= 0")
    @Digits(integer = 15, fraction = 2)
    private BigDecimal amount;

    @NotNull(message = "Phương pháp phân bổ gợi ý là bắt buộc")
    private AllocationMethod suggestedAllocation;

    @Size(max = 2000)
    private String note;

    @Size(max = 100)
    private String refNo;

    @Size(max = 100)
    private String costCenter;

    /** Tag tuỳ chọn để gắn vào sản phẩm/lô */
    private Long productId;
    private Long productBatchId;
}
