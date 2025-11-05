package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.repository.LocationRepository;
import ext.vnua.veterinary_beapp.modules.material.service.LocationCapacityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementation of LocationCapacityService
 * Manages warehouse location capacity in real-time
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationCapacityServiceImpl implements LocationCapacityService {

    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public void addBatchToLocation(Long locationId, BigDecimal quantity) {
        if (locationId == null || quantity == null) {
            log.debug("LocationId or quantity is null, skipping capacity update");
            return;
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("Quantity is zero or negative, skipping capacity update");
            return;
        }

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new DataExistException("Không tìm thấy vị trí kho với ID: " + locationId));

        Double currentCapacity = location.getCurrentCapacity() != null 
                ? location.getCurrentCapacity() 
                : 0.0;
        
        Double addedQuantity = quantity.doubleValue();
        Double newCapacity = currentCapacity + addedQuantity;

        // Check if exceeds max capacity
        if (location.getMaxCapacity() != null && newCapacity > location.getMaxCapacity()) {
            Double available = location.getMaxCapacity() - currentCapacity;
            throw new MyCustomException(String.format(
                    "Vị trí kho '%s' không đủ sức chứa. Còn trống: %.3f, Cần: %.3f",
                    location.getLocationCode(), available, addedQuantity));
        }

        location.setCurrentCapacity(newCapacity);
        
        // Update availability status if needed
        if (location.getMaxCapacity() != null && newCapacity >= location.getMaxCapacity()) {
            location.setIsAvailable(false);
            log.info("Vị trí kho '{}' đã đầy, đặt trạng thái không khả dụng", location.getLocationCode());
        }

        locationRepository.save(location);

        log.info("Đã thêm {} vào vị trí '{}'. Capacity hiện tại: {} / {}",
                addedQuantity, location.getLocationCode(), newCapacity, location.getMaxCapacity());
    }

    @Override
    @Transactional
    public void removeBatchFromLocation(Long locationId, BigDecimal quantity) {
        if (locationId == null || quantity == null) {
            log.debug("LocationId or quantity is null, skipping capacity update");
            return;
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("Quantity is zero or negative, skipping capacity update");
            return;
        }

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new DataExistException("Không tìm thấy vị trí kho với ID: " + locationId));

        Double currentCapacity = location.getCurrentCapacity() != null 
                ? location.getCurrentCapacity() 
                : 0.0;
        
        Double removedQuantity = quantity.doubleValue();
        Double newCapacity = currentCapacity - removedQuantity;

        // Ensure capacity doesn't go negative
        if (newCapacity < 0) {
            log.warn("Capacity would go negative for location '{}', setting to 0", location.getLocationCode());
            newCapacity = 0.0;
        }

        location.setCurrentCapacity(newCapacity);

        // Update availability status - location becomes available if it was full
        if (location.getMaxCapacity() != null && newCapacity < location.getMaxCapacity()) {
            if (!location.getIsAvailable()) {
                location.setIsAvailable(true);
                log.info("Vị trí kho '{}' có chỗ trống, đặt lại trạng thái khả dụng", location.getLocationCode());
            }
        }

        locationRepository.save(location);

        log.info("Đã bớt {} từ vị trí '{}'. Capacity hiện tại: {} / {}",
                removedQuantity, location.getLocationCode(), newCapacity, location.getMaxCapacity());
    }

    @Override
    @Transactional
    public void moveBatch(Long fromLocationId, Long toLocationId, BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("Invalid quantity for move operation, skipping");
            return;
        }

        // Remove from old location if exists
        if (fromLocationId != null) {
            removeBatchFromLocation(fromLocationId, quantity);
        }

        // Add to new location if exists
        if (toLocationId != null) {
            addBatchToLocation(toLocationId, quantity);
        }

        log.info("Đã di chuyển {} từ vị trí {} đến vị trí {}", 
                quantity, fromLocationId, toLocationId);
    }

    @Override
    @Transactional
    public void updateBatchQuantity(Long locationId, BigDecimal oldQuantity, BigDecimal newQuantity) {
        if (locationId == null) {
            log.debug("LocationId is null, skipping capacity update");
            return;
        }

        if (oldQuantity == null) oldQuantity = BigDecimal.ZERO;
        if (newQuantity == null) newQuantity = BigDecimal.ZERO;

        BigDecimal difference = newQuantity.subtract(oldQuantity);

        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            log.debug("No quantity change, skipping capacity update");
            return;
        }

        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            // Quantity increased - add to location
            addBatchToLocation(locationId, difference);
        } else {
            // Quantity decreased - remove from location
            removeBatchFromLocation(locationId, difference.abs());
        }

        log.info("Đã cập nhật số lượng tại vị trí {}: {} -> {} (thay đổi: {})",
                locationId, oldQuantity, newQuantity, difference);
    }

    @Override
    @Transactional
    public void recalculateLocationCapacity(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new DataExistException("Không tìm thấy vị trí kho với ID: " + locationId));

        // Calculate total from all batch items in this location (updated for new structure)
        Double totalOccupied = 0.0;
        
        if (location.getMaterialBatches() != null && !location.getMaterialBatches().isEmpty()) {
            totalOccupied = location.getMaterialBatches().stream()
                    .filter(batch -> batch.getBatchItems() != null)
                    .flatMap(batch -> batch.getBatchItems().stream())
                    .filter(item -> item.getLocation() != null && item.getLocation().getId().equals(locationId))
                    .map(item -> item.getCurrentQuantity())
                    .filter(qty -> qty != null)
                    .mapToDouble(BigDecimal::doubleValue)
                    .sum();
        }

        Double oldCapacity = location.getCurrentCapacity();
        location.setCurrentCapacity(totalOccupied);

        // Update availability
        if (location.getMaxCapacity() != null) {
            location.setIsAvailable(totalOccupied < location.getMaxCapacity());
        }

        locationRepository.save(location);

        int totalItems = location.getMaterialBatches() != null 
            ? location.getMaterialBatches().stream()
                .mapToInt(batch -> batch.getBatchItems() != null ? batch.getBatchItems().size() : 0)
                .sum()
            : 0;

        log.info("Đã tính lại capacity cho vị trí '{}': {} -> {} (từ {} items)",
                location.getLocationCode(), oldCapacity, totalOccupied, totalItems);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAvailableCapacity(Long locationId, BigDecimal quantity) {
        if (locationId == null || quantity == null) {
            return false;
        }

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new DataExistException("Không tìm thấy vị trí kho với ID: " + locationId));

        // If no max capacity set, assume unlimited
        if (location.getMaxCapacity() == null) {
            return true;
        }

        Double currentCapacity = location.getCurrentCapacity() != null 
                ? location.getCurrentCapacity() 
                : 0.0;
        
        Double availableCapacity = location.getMaxCapacity() - currentCapacity;
        
        return quantity.doubleValue() <= availableCapacity;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAvailableCapacity(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new DataExistException("Không tìm thấy vị trí kho với ID: " + locationId));

        if (location.getMaxCapacity() == null) {
            return null; // Unlimited capacity
        }

        Double currentCapacity = location.getCurrentCapacity() != null 
                ? location.getCurrentCapacity() 
                : 0.0;

        Double available = location.getMaxCapacity() - currentCapacity;
        return Math.max(0.0, available); // Ensure non-negative
    }

    @Override
    @Transactional(readOnly = true)
    public Double getOccupancyPercentage(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new DataExistException("Không tìm thấy vị trí kho với ID: " + locationId));

        if (location.getMaxCapacity() == null || location.getMaxCapacity() == 0) {
            return 0.0;
        }

        Double currentCapacity = location.getCurrentCapacity() != null 
                ? location.getCurrentCapacity() 
                : 0.0;

        return (currentCapacity / location.getMaxCapacity()) * 100.0;
    }
}
