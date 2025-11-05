package ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO để tạo mới kết quả kiểm nghiệm hoạt chất
 * Sử dụng cho việc nhập COA và KQPT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestResultRequest {

    /**
     * ID của MaterialBatchItem
     */
    @NotNull(message = "Batch Item ID không được để trống")
    private Long batchItemId;

    /**
     * ID của ActiveIngredient từ Material master data
     */
    @NotNull(message = "Active Ingredient ID không được để trống")
    private Long activeIngredientId;

    /**
     * Hàm lượng COA (Certificate of Analysis - từ nhà cung cấp)
     */
    @NotNull(message = "Hàm lượng COA không được để trống")
    private BigDecimal coaContentValue;

    /**
     * Đơn vị hàm lượng COA
     */
    @NotNull(message = "Đơn vị COA không được để trống")
    private String coaContentUnit;

    /**
     * Hàm lượng KQPT (Kết quả phân tích thực tế - từ lab)
     */
    private BigDecimal testContentValue;

    /**
     * Đơn vị hàm lượng test
     */
    private String testContentUnit;

    /**
     * Ngày kiểm tra
     */
    private LocalDate testDate;

    /**
     * Phương pháp kiểm tra
     */
    private String testMethod;

    /**
     * Ghi chú về COA
     */
    private String coaNotes;

    /**
     * Ghi chú về kết quả test
     */
    private String testNotes;
}
