package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.LocationDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.location.CreateLocationRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.location.UpdateLocationRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {WarehouseMapper.class})
public interface LocationMapper {
    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    @Mapping(source = "warehouse", target = "warehouseDto")
    LocationDto toLocationDto(Location location);

    @Mapping(source = "warehouseDto", target = "warehouse")
    Location toLocation(LocationDto locationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "materialBatches", ignore = true)
    @Mapping(target = "isAvailable", ignore = true) // Will be set to true by default in service
    Location toCreateLocation(CreateLocationRequest request);

    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "materialBatches", ignore = true)
    Location toUpdateLocation(UpdateLocationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "materialBatches", ignore = true)
    void updateLocationFromRequest(UpdateLocationRequest request, @MappingTarget Location location);

    @AfterMapping
    default void afterMappingCreate(@MappingTarget Location location, CreateLocationRequest request) {
        // Set default values if not provided
        if (location.getCurrentCapacity() == null) {
            location.setCurrentCapacity(0.0);
        }
        if (location.getIsAvailable() == null) {
            location.setIsAvailable(true);
        }
    }

    @AfterMapping
    default void afterMappingUpdate(@MappingTarget Location location, UpdateLocationRequest request) {
        // Ensure currentCapacity is not null
        if (location.getCurrentCapacity() == null) {
            location.setCurrentCapacity(0.0);
        }
    }
}