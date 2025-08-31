package ext.vnua.veterinary_beapp.modules.material.dto.request.location;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateLocationRequest {

    @NotNull(message = "ID kho không được để trống")
    @Min(value = 1, message = "ID kho phải lớn hơn 0")
    private Long warehouseId;

    @NotBlank(message = "Mã vị trí không được để trống")
    @Size(max = 50, message = "Mã vị trí không được vượt quá 50 ký tự")
    private String locationCode;

    @Size(max = 50, message = "Thông tin kệ không được vượt quá 50 ký tự")
    private String shelf;

    @Size(max = 50, message = "Thông tin tầng không được vượt quá 50 ký tự")
    private String floor;

    @Size(max = 255, message = "Chi tiết vị trí không được vượt quá 255 ký tự")
    private String positionDetail;

    @DecimalMin(value = "0.1", message = "Sức chứa tối đa phải lớn hơn 0")
    private Double maxCapacity;

    @DecimalMin(value = "0.0", message = "Sức chứa hiện tại không được âm")
    private Double currentCapacity;
}
