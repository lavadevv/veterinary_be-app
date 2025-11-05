package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.LocationDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.location.CreateLocationRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.location.UpdateLocationRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomLocationQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface LocationService {
    Page<Location> getAllLocation(CustomLocationQuery.LocationFilterParam param, PageRequest pageRequest);
    LocationDto selectLocationById(Long id);
    LocationDto selectLocationByCode(String locationCode);
    List<LocationDto> selectLocationsByWarehouse(Long warehouseId);

    LocationDto createLocation(CreateLocationRequest request);
    LocationDto updateLocation(UpdateLocationRequest request);

    void deleteLocation(Long id);
    List<LocationDto> deleteAllIdLocations(List<Long> ids);

    // Additional business methods
    void updateCurrentCapacity(Long locationId, Double newCapacity);
    void toggleAvailability(Long locationId);
    List<LocationDto> getAvailableLocationsByWarehouse(Long warehouseId);
    
    // Capacity management methods
    void recalculateLocationCapacity(Long locationId);
    Double getAvailableCapacity(Long locationId);
    Double getOccupancyPercentage(Long locationId);
}
