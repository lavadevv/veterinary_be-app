package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.LocationDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.location.CreateLocationRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.location.UpdateLocationRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.LocationMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.model.Warehouse;
import ext.vnua.veterinary_beapp.modules.material.repository.LocationRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.WarehouseRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomLocationQuery;
import ext.vnua.veterinary_beapp.modules.material.service.LocationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;
    private final WarehouseRepository warehouseRepository;
    private final LocationMapper locationMapper;

    @Override
    public Page<Location> getAllLocation(CustomLocationQuery.LocationFilterParam param, PageRequest pageRequest) {
        Specification<Location> specification = CustomLocationQuery.getFilterLocation(param);
        return locationRepository.findAll(specification, pageRequest);
    }

    @Override
    public LocationDto selectLocationById(Long id) {
        Optional<Location> locationOptional = locationRepository.findById(id);
        if (locationOptional.isEmpty()) {
            throw new DataExistException("Vị trí không tồn tại");
        }
        Location location = locationOptional.get();
        return locationMapper.toLocationDto(location);
    }

    @Override
    public LocationDto selectLocationByCode(String locationCode) {
        Optional<Location> locationOptional = locationRepository.findByLocationCode(locationCode);
        if (locationOptional.isEmpty()) {
            throw new DataExistException("Mã vị trí không tồn tại");
        }
        Location location = locationOptional.get();
        return locationMapper.toLocationDto(location);
    }

    @Override
    public List<LocationDto> selectLocationsByWarehouse(Long warehouseId) {
        List<Location> locations = locationRepository.findByWarehouseId(warehouseId);
        return locations.stream()
                .map(locationMapper::toLocationDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "Location", description = "Tạo mới vị trí")
    public LocationDto createLocation(CreateLocationRequest request) {
        // Validate warehouse exists
        Warehouse warehouse = warehouseRepository.findById(String.valueOf(request.getWarehouseId()))
                .orElseThrow(() -> new DataExistException("Kho không tồn tại"));

        // Validate location code is unique within warehouse
        Optional<Location> existingLocation = locationRepository
                .findByLocationCodeAndWarehouseId(request.getLocationCode(), request.getWarehouseId());
        if (existingLocation.isPresent()) {
            throw new DataExistException("Mã vị trí đã tồn tại trong kho này");
        }

        // Validate capacity
        if (request.getMaxCapacity() != null && request.getMaxCapacity() <= 0) {
            throw new MyCustomException("Sức chứa tối đa phải lớn hơn 0");
        }

        if (request.getCurrentCapacity() != null && request.getMaxCapacity() != null
                && request.getCurrentCapacity() > request.getMaxCapacity()) {
            throw new MyCustomException("Sức chứa hiện tại không được vượt quá sức chứa tối đa");
        }

        try {
            Location location = locationMapper.toCreateLocation(request);
            location.setWarehouse(warehouse);
            location.setIsAvailable(true);

            // Set default current capacity if not provided
            if (location.getCurrentCapacity() == null) {
                location.setCurrentCapacity(0.0);
            }

            return locationMapper.toLocationDto(locationRepository.saveAndFlush(location));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm vị trí");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "Location", description = "Cập nhật vị trí")
    public LocationDto updateLocation(UpdateLocationRequest request) {
        Optional<Location> locationOptional = locationRepository.findById(request.getId());
        if (locationOptional.isEmpty()) {
            throw new DataExistException("Vị trí không tồn tại");
        }

        Location existingLocation = locationOptional.get();

        // Validate warehouse exists if changed
        Warehouse warehouse = warehouseRepository.findById(String.valueOf(request.getWarehouseId()))
                .orElseThrow(() -> new DataExistException("Kho không tồn tại"));

        // Validate location code is unique within warehouse (excluding current location)
        if (!existingLocation.getLocationCode().equals(request.getLocationCode()) ||
                !existingLocation.getWarehouse().getId().equals(request.getWarehouseId())) {
            Optional<Location> duplicateLocation = locationRepository
                    .findByLocationCodeAndWarehouseIdAndIdNot(
                            request.getLocationCode(),
                            request.getWarehouseId(),
                            request.getId());
            if (duplicateLocation.isPresent()) {
                throw new DataExistException("Mã vị trí đã tồn tại trong kho này");
            }
        }

        // Validate capacity
        if (request.getMaxCapacity() != null && request.getMaxCapacity() <= 0) {
            throw new MyCustomException("Sức chứa tối đa phải lớn hơn 0");
        }

        if (request.getCurrentCapacity() != null && request.getMaxCapacity() != null
                && request.getCurrentCapacity() > request.getMaxCapacity()) {
            throw new MyCustomException("Sức chứa hiện tại không được vượt quá sức chứa tối đa");
        }

        try {
            Location location = locationMapper.toUpdateLocation(request);
            location.setWarehouse(warehouse);

            return locationMapper.toLocationDto(locationRepository.saveAndFlush(location));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật vị trí");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Location", description = "Xóa vị trí")
    public void deleteLocation(Long id) {
        Optional<Location> locationOptional = locationRepository.findById(id);
        if (locationOptional.isEmpty()) {
            throw new DataExistException("Vị trí không tồn tại");
        }

        Location location = locationOptional.get();

        // Check if location has material batches
        if (location.getMaterialBatches() != null && !location.getMaterialBatches().isEmpty()) {
            throw new MyCustomException("Không thể xóa vị trí đang chứa vật liệu");
        }

        try {
            locationRepository.deleteById(id);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa vị trí");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Location", description = "Xóa danh sách vị trí")
    public List<LocationDto> deleteAllIdLocations(List<Long> ids) {
        List<LocationDto> locationDtos = new ArrayList<>();
        for (Long id : ids) {
            Optional<Location> optionalLocation = locationRepository.findById(id);
            if (optionalLocation.isPresent()) {
                Location location = optionalLocation.get();

                // Check if location has material batches
                if (location.getMaterialBatches() != null && !location.getMaterialBatches().isEmpty()) {
                    throw new MyCustomException("Không thể xóa vị trí đang chứa vật liệu: " + location.getLocationCode());
                }

                locationDtos.add(locationMapper.toLocationDto(location));
                locationRepository.delete(location);
            } else {
                throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách vị trí!");
            }
        }
        return locationDtos;
    }

    @Override
    @Transactional
    public void updateCurrentCapacity(Long locationId, Double newCapacity) {
        Optional<Location> locationOptional = locationRepository.findById(locationId);
        if (locationOptional.isEmpty()) {
            throw new DataExistException("Vị trí không tồn tại");
        }

        Location location = locationOptional.get();

        if (newCapacity < 0) {
            throw new MyCustomException("Sức chứa hiện tại không được âm");
        }

        if (location.getMaxCapacity() != null && newCapacity > location.getMaxCapacity()) {
            throw new MyCustomException("Sức chứa hiện tại không được vượt quá sức chứa tối đa");
        }

        location.setCurrentCapacity(newCapacity);
        // Update availability based on capacity
        location.setIsAvailable(newCapacity < location.getMaxCapacity());

        locationRepository.saveAndFlush(location);
    }

    @Override
    @Transactional
    public void toggleAvailability(Long locationId) {
        Optional<Location> locationOptional = locationRepository.findById(locationId);
        if (locationOptional.isEmpty()) {
            throw new DataExistException("Vị trí không tồn tại");
        }

        Location location = locationOptional.get();
        location.setIsAvailable(!location.getIsAvailable());

        locationRepository.saveAndFlush(location);
    }

    @Override
    public List<LocationDto> getAvailableLocationsByWarehouse(Long warehouseId) {
        List<Location> locations = locationRepository.findByWarehouseIdAndIsAvailableTrue(warehouseId);
        return locations.stream()
                .map(locationMapper::toLocationDto)
                .collect(java.util.stream.Collectors.toList());
    }
}
