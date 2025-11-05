package ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO để cập nhật kết quả kiểm nghiệm hoạt chất
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTestResultRequest {

    /**
     * Hàm lượng COA (Certificate of Analysis - từ nhà cung cấp)
     */
    private BigDecimal coaContentValue;

    /**
     * Đơn vị hàm lượng COA
     */
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
