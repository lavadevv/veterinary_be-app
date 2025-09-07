package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WarehouseDto {
    private Long id;
    private String warehouseCode;
    private String warehouseName;
    private String warehouseType;
    private String address;
    private String managerName;
    private String temperatureRange;
    private String humidityRange;
    private String specialConditions;
    private Boolean isActive;

    @Override
    public String toString() {
        return String.format(
                "Kho:\n" +
                        "   - ID: %d\n" +
                        "   - Mã kho: %s\n" +
                        "   - Tên kho: %s\n" +
                        "   - Loại kho: %s\n" +
                        "   - Địa chỉ: %s\n" +
                        "   - Người quản lý: %s\n" +
                        "   - Nhiệt độ: %s\n" +
                        "   - Độ ẩm: %s\n" +
                        "   - Điều kiện đặc biệt: %s\n" +
                        "   - Trạng thái: %s\n",
                id,
                warehouseCode,
                warehouseName,
                warehouseType,
                address,
                managerName,
                temperatureRange,
                humidityRange,
                specialConditions,
                Boolean.TRUE.equals(isActive) ? "Đang hoạt động" : "Ngừng hoạt động"
        );
    }

}