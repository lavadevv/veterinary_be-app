package ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch;

import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateMaterialBatchRequest {

    @NotNull(message = "ID không được để trống")
    private Long id;

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

    @DecimalMin(value = "0.0", message = "Thuế không được âm")
    @DecimalMax(value = "100.0", message = "Thuế không được vượt quá 100%")
    @Digits(integer = 3, fraction = 4, message = "Thuế không hợp lệ")
    private BigDecimal taxPercent;

    @Min(value = 1, message = "ID nhà cung cấp phải lớn hơn 0")
    private Long supplierId;

    @Min(value = 1, message = "ID nhà sản xuất phải lớn hơn 0")
    private Long manufacturerId;

    @Size(max = 100, message = "Xuất xứ không được vượt quá 100 ký tự")
    private String countryOfOrigin;

    @Size(max = 100, message = "Số hóa đơn nhập không được vượt quá 100 ký tự")
    private String invoiceNumber;

    @NotNull(message = "Trạng thái kiểm nghiệm không được để trống")
    private TestStatus testStatus;

    @NotNull(message = "Trạng thái sử dụng không được để trống")
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

    // Active ingredients COA/KQPT data
    private List<ActiveIngredientInput> activeIngredients;

    @Data
    public static class ActiveIngredientInput {
        @NotNull(message = "ID hoạt chất không được để trống")
        private Long activeIngredientId;

        @NotNull(message = "Hàm lượng COA không được để trống")
        @DecimalMin(value = "0.0", message = "Hàm lượng COA không được âm")
        private BigDecimal coaContent;

        @DecimalMin(value = "0.0", message = "Hàm lượng thực tế không được âm")
        private BigDecimal actualContent;

        @Size(max = 500, message = "Ghi chú hoạt chất không được vượt quá 500 ký tự")
        private String notes;
    }
}
