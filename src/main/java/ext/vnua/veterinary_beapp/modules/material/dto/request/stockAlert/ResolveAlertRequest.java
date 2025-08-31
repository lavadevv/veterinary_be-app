package ext.vnua.veterinary_beapp.modules.material.dto.request.stockAlert;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResolveAlertRequest {

    @NotNull(message = "ID cảnh báo không được để trống")
    @Min(value = 1, message = "ID cảnh báo phải lớn hơn 0")
    private Long alertId;

    @NotNull(message = "ID người giải quyết không được để trống")
    @Min(value = 1, message = "ID người giải quyết phải lớn hơn 0")
    private Long userId;

    @Size(max = 1000, message = "Ghi chú giải quyết không được vượt quá 1000 ký tự")
    private String resolutionNotes;
}
