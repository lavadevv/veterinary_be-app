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
}