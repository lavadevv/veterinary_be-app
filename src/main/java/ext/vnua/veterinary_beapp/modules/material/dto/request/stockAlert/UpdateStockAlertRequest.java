package ext.vnua.veterinary_beapp.modules.material.dto.request.stockAlert;

import ext.vnua.veterinary_beapp.modules.material.model.StockAlert;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateStockAlertRequest {

    @NotNull(message = "ID không được để trống")
    @Min(value = 1, message = "ID phải lớn hơn 0")
    private Long id;

    @Min(value = 1, message = "ID vật liệu phải lớn hơn 0")
    private Long materialId;

    @Min(value = 1, message = "ID lô vật liệu phải lớn hơn 0")
    private Long materialBatchId;

    private StockAlert.AlertType alertType;

    @Size(max = 1000, message = "Thông điệp cảnh báo không được vượt quá 1000 ký tự")
    private String alertMessage;

    private Boolean isResolved;

    @Size(max = 1000, message = "Ghi chú giải quyết không được vượt quá 1000 ký tự")
    private String resolutionNotes;
}
