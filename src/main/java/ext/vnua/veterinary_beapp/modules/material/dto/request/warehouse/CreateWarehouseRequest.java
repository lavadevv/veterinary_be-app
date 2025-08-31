package ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateWarehouseRequest {

    @NotBlank(message = "Mã kho không được để trống")
    @Size(max = 50, message = "Mã kho không được vượt quá 50 ký tự")
    private String warehouseCode;

    @NotBlank(message = "Tên kho không được để trống")
    @Size(max = 255, message = "Tên kho không được vượt quá 255 ký tự")
    private String warehouseName;

    @Size(max = 100, message = "Loại kho không được vượt quá 100 ký tự")
    private String warehouseType;

    private String address;

    @Size(max = 100, message = "Tên quản lý không được vượt quá 100 ký tự")
    private String managerName;

    @Size(max = 50, message = "Khoảng nhiệt độ không được vượt quá 50 ký tự")
    private String temperatureRange;

    @Size(max = 50, message = "Khoảng độ ẩm không được vượt quá 50 ký tự")
    private String humidityRange;

    private String specialConditions;
}
