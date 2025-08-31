package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import lombok.Data;

@Data
public class LocationDto {
    private Long id;
    private WarehouseDto warehouseDto;
    private String locationCode;
    private String shelf;
    private String floor;
    private String positionDetail;
    private Double maxCapacity;
    private Double currentCapacity;
    private Boolean isAvailable;
}