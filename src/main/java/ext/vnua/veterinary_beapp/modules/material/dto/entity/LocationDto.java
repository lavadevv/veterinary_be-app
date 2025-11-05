package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import lombok.Data;

@Data
public class LocationDto {
    private Long id;
    private Long warehouseId;
    private String locationCode;
    private String shelf;
    private String floor;
    private String positionDetail;
    private Double maxCapacity;
    private Double currentCapacity;
    private Boolean isAvailable;

    @Override
    public String toString() {
        return String.format(
                "Vị trí trong kho:\n" +
                        "   - ID: %d\n" +
                        "   - Mã vị trí: %s\n" +
                        "   - Kệ: %s | Tầng: %s | Chi tiết: %s\n" +
                        "   - Sức chứa tối đa: %.2f\n" +
                        "   - Sức chứa hiện tại: %.2f\n" +
                        "   - Trạng thái: %s\n" +
                        "   - Kho: %s\n",
                id,
                locationCode,
                shelf,
                floor,
                positionDetail,
                maxCapacity != null ? maxCapacity : 0.0,
                currentCapacity != null ? currentCapacity : 0.0,
                Boolean.TRUE.equals(isAvailable) ? "Còn trống" : "Đã đầy/không khả dụng",
                warehouseId != null ? warehouseId : "Không rõ"
        );
    }

}