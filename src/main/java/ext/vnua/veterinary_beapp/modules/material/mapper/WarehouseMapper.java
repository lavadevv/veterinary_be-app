package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.WarehouseDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse.CreateWarehouseRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse.UpdateWarehouseRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Warehouse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    WarehouseMapper INSTANCE = Mappers.getMapper(WarehouseMapper.class);

    WarehouseDto toWarehouseDto(Warehouse warehouse);
    Warehouse toWarehouse(WarehouseDto warehouseDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "isActive", ignore = true) // Will be set to true by default in service
    Warehouse toCreateWarehouse(CreateWarehouseRequest request);

    @Mapping(target = "locations", ignore = true)
    Warehouse toUpdateWarehouse(UpdateWarehouseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "locations", ignore = true)
    void updateWarehouseFromRequest(UpdateWarehouseRequest request, @MappingTarget Warehouse warehouse);

    @AfterMapping
    default void afterMappingCreate(@MappingTarget Warehouse warehouse, CreateWarehouseRequest request) {
        // Set default values if not provided
        if (warehouse.getIsActive() == null) {
            warehouse.setIsActive(true);
        }
    }
}