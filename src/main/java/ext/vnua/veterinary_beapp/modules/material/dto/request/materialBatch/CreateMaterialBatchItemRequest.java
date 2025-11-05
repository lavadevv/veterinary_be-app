package ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaterialBatchItemRequest {

    @NotNull(message = "Material ID không được để trống")
    private Long materialId;

    private Long locationId; // Optional - override batch's default location

    private LocalDate manufacturingDate;

    private LocalDate expiryDate;

    @NotNull(message = "Số lượng nhận không được để trống")
    @DecimalMin(value = "0.001", message = "Số lượng nhận phải lớn hơn 0")
    private BigDecimal receivedQuantity;

    private BigDecimal currentQuantity; // If null, will default to receivedQuantity

    private BigDecimal unitPrice;

    private BigDecimal taxPercent;

    private BigDecimal subtotalAmount;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    @Size(max = 50, message = "Test status không được vượt quá 50 ký tự")
    private String testStatus; // CHO_KIEM_NGHIEM, DA_KIEM_NGHIEM, DAT, KHONG_DAT, etc.

    @Size(max = 50, message = "Usage status không được vượt quá 50 ký tự")
    private String usageStatus; // CACH_LY, SAN_SANG, DANG_SU_DUNG, DA_SU_DUNG, etc.

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;
}
