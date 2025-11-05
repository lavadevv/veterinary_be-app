package ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch;

import ext.vnua.veterinary_beapp.modules.material.enums.BatchStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO for creating a MaterialBatch container (header/envelope only).
 * Items will be added separately via MaterialBatchItem endpoints.
 */
@Data
public class CreateMaterialBatchContainerRequest {

    @NotBlank(message = "Số lô không được để trống")
    @Size(max = 100, message = "Số lô không được vượt quá 100 ký tự")
    private String batchNumber;

    @Size(max = 100, message = "Mã lô nội bộ không được vượt quá 100 ký tự")
    private String internalBatchCode;

    @NotNull(message = "Ngày nhận không được để trống")
    private LocalDate receivedDate;

    @Size(max = 100, message = "Số hóa đơn nhập không được vượt quá 100 ký tự")
    private String invoiceNumber;

    @Size(max = 100, message = "Xuất xứ không được vượt quá 100 ký tự")
    private String countryOfOrigin;

    @NotNull(message = "Trạng thái lô không được để trống")
    private BatchStatus batchStatus;

    @NotNull(message = "ID nhà cung cấp không được để trống")
    @Min(value = 1, message = "ID nhà cung cấp phải lớn hơn 0")
    private Long supplierId;

    @NotNull(message = "ID nhà sản xuất không được để trống")
    @Min(value = 1, message = "ID nhà sản xuất phải lớn hơn 0")
    private Long manufacturerId;

    @Min(value = 1, message = "ID vị trí phải lớn hơn 0")
    private Long locationId; // Default location for items (optional)

    @Size(max = 2000, message = "Ghi chú không được vượt quá 2000 ký tự")
    private String notes;
}
