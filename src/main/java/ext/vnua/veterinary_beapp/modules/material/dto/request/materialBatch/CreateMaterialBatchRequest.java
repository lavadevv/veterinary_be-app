package ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch;

import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateMaterialBatchRequest {

    @NotNull(message = "ID vật liệu không được để trống")
    @Min(value = 1, message = "ID vật liệu phải lớn hơn 0")
    private Long materialId;

    @Min(value = 1, message = "ID vị trí phải lớn hơn 0")
    private Long locationId;

    @NotBlank(message = "Số lô không được để trống")
    @Size(max = 100, message = "Số lô không được vượt quá 100 ký tự")
    private String batchNumber;

    @Size(max = 100, message = "Mã lô nội bộ không được vượt quá 100 ký tự")
    private String internalBatchCode;

    @Size(max = 100, message = "Số lô nhà sản xuất không được vượt quá 100 ký tự")
    private String manufacturerBatchNumber;

    private LocalDate manufacturingDate;

    private LocalDate expiryDate;

    @NotNull(message = "Ngày nhận không được để trống")
    private LocalDate receivedDate;

    @NotNull(message = "Số lượng nhận không được để trống")
    @DecimalMin(value = "0.001", message = "Số lượng nhận phải lớn hơn 0")
    @Digits(integer = 12, fraction = 3, message = "Số lượng nhận không hợp lệ")
    private BigDecimal receivedQuantity;

    @DecimalMin(value = "0.0", message = "Số lượng hiện tại không được âm")
    @Digits(integer = 12, fraction = 3, message = "Số lượng hiện tại không hợp lệ")
    private BigDecimal currentQuantity;

    @DecimalMin(value = "0.0", message = "Đơn giá không được âm")
    @Digits(integer = 12, fraction = 2, message = "Đơn giá không hợp lệ")
    private BigDecimal unitPrice;

    private TestStatus testStatus;

    private UsageStatus usageStatus;

    @Size(max = 100, message = "Số COA không được vượt quá 100 ký tự")
    private String coaNumber;

    @Size(max = 100, message = "Số báo cáo kiểm nghiệm không được vượt quá 100 ký tự")
    private String testReportNumber;

    private String testResults; // JSON format

    @Size(max = 1000, message = "Lý do cách ly không được vượt quá 1000 ký tự")
    private String quarantineReason;

    @Size(max = 500, message = "Đường dẫn file COA không được vượt quá 500 ký tự")
    private String coaFilePath;

    @Size(max = 500, message = "Đường dẫn file MSDS không được vượt quá 500 ký tự")
    private String msdsFilePath;

    @Size(max = 500, message = "Đường dẫn chứng nhận kiểm nghiệm không được vượt quá 500 ký tự")
    private String testCertificatePath;

    @Size(max = 2000, message = "Ghi chú không được vượt quá 2000 ký tự")
    private String notes;
}