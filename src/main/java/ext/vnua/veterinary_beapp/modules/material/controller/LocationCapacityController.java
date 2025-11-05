package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.LocationDto;
import ext.vnua.veterinary_beapp.modules.material.service.LocationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for managing warehouse location capacity
 * Provides endpoints for checking capacity status and recalculating capacity
 */
@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
@Api(tags = "Location Capacity Management")
public class LocationCapacityController {

    private final LocationService locationService;

    @ApiOperation(value = "Lấy thông tin sức chứa đầy đủ của vị trí", 
                  notes = "Trả về thông tin chi tiết về sức chứa bao gồm: sức chứa tối đa, đang sử dụng, còn trống và tỷ lệ lấp đầy")
    @GetMapping("/{id}/capacity-info")
    public ResponseEntity<?> getLocationCapacityInfo(
            @ApiParam(value = "ID của vị trí", required = true, example = "1")
            @PathVariable("id") Long id) {
        
        LocationDto location = locationService.selectLocationById(id);
        Double availableCapacity = locationService.getAvailableCapacity(id);
        Double occupancyPercentage = locationService.getOccupancyPercentage(id);

        return ResponseEntity.ok(Map.of(
                "id", location.getId(),
                "locationCode", location.getLocationCode(),
                "warehouseId", location.getWarehouseId() != null ? location.getWarehouseId() : 0L,
                "maxCapacity", location.getMaxCapacity() != null ? location.getMaxCapacity() : 0.0,
                "currentCapacity", location.getCurrentCapacity() != null ? location.getCurrentCapacity() : 0.0,
                "availableCapacity", availableCapacity != null ? availableCapacity : 0.0,
                "occupancyPercentage", occupancyPercentage != null ? occupancyPercentage : 0.0,
                "isAvailable", location.getIsAvailable(),
                "status", getCapacityStatus(occupancyPercentage)
        ));
    }

    @ApiOperation(value = "Tính lại sức chứa của vị trí", 
                  notes = "Tính toán lại tổng sức chứa đang sử dụng dựa trên tất cả các lô vật liệu hiện có tại vị trí này. Hữu ích khi phát hiện sai lệch dữ liệu.")
    @PostMapping("/{id}/recalculate-capacity")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<?> recalculateCapacity(
            @ApiParam(value = "ID của vị trí", required = true, example = "1")
            @PathVariable("id") Long id) {
        
        locationService.recalculateLocationCapacity(id);
        LocationDto location = locationService.selectLocationById(id);
        Double availableCapacity = locationService.getAvailableCapacity(id);
        Double occupancyPercentage = locationService.getOccupancyPercentage(id);
        
        return ResponseEntity.ok(Map.of(
                "message", "Đã tính lại sức chứa thành công",
                "locationCode", location.getLocationCode(),
                "currentCapacity", location.getCurrentCapacity() != null ? location.getCurrentCapacity() : 0.0,
                "availableCapacity", availableCapacity != null ? availableCapacity : 0.0,
                "occupancyPercentage", occupancyPercentage != null ? occupancyPercentage : 0.0,
                "status", getCapacityStatus(occupancyPercentage)
        ));
    }

    @ApiOperation(value = "Lấy sức chứa còn trống của vị trí", 
                  notes = "Trả về số lượng sức chứa còn có thể sử dụng tại vị trí này")
    @GetMapping("/{id}/available-capacity")
    public ResponseEntity<?> getAvailableCapacity(
            @ApiParam(value = "ID của vị trí", required = true, example = "1")
            @PathVariable("id") Long id) {
        
        Double availableCapacity = locationService.getAvailableCapacity(id);
        LocationDto location = locationService.selectLocationById(id);
        
        return ResponseEntity.ok(Map.of(
                "locationId", id,
                "locationCode", location.getLocationCode(),
                "availableCapacity", availableCapacity != null ? availableCapacity : 0.0,
                "maxCapacity", location.getMaxCapacity() != null ? location.getMaxCapacity() : 0.0,
                "displayText", availableCapacity == null 
                        ? "Không giới hạn" 
                        : String.format("%.2f / %.2f đơn vị", availableCapacity, location.getMaxCapacity()),
                "hasSpace", availableCapacity == null || availableCapacity > 0
        ));
    }

    @ApiOperation(value = "Lấy tỷ lệ lấp đầy của vị trí", 
                  notes = "Trả về phần trăm sức chứa đã sử dụng (0-100%)")
    @GetMapping("/{id}/occupancy-percentage")
    public ResponseEntity<?> getOccupancyPercentage(
            @ApiParam(value = "ID của vị trí", required = true, example = "1")
            @PathVariable("id") Long id) {
        
        Double percentage = locationService.getOccupancyPercentage(id);
        LocationDto location = locationService.selectLocationById(id);
        String status = getCapacityStatus(percentage);
        
        return ResponseEntity.ok(Map.of(
                "locationId", id,
                "locationCode", location.getLocationCode(),
                "percentage", percentage != null ? percentage : 0.0,
                "status", status,
                "color", getStatusColor(percentage),
                "isNearFull", percentage != null && percentage >= 80.0
        ));
    }

    /**
     * Determine capacity status based on occupancy percentage
     */
    private String getCapacityStatus(Double percentage) {
        if (percentage == null || percentage == 0.0) {
            return "Trống";
        }
        if (percentage >= 95.0) return "Đầy";
        if (percentage >= 80.0) return "Gần đầy";
        if (percentage >= 60.0) return "Đang đầy dần";
        if (percentage >= 30.0) return "Vừa phải";
        if (percentage >= 10.0) return "Còn nhiều chỗ";
        return "Gần trống";
    }

    /**
     * Get color code for status visualization
     */
    private String getStatusColor(Double percentage) {
        if (percentage == null || percentage == 0.0) {
            return "gray";
        }
        if (percentage >= 90.0) return "red";
        if (percentage >= 70.0) return "orange";
        if (percentage >= 50.0) return "yellow";
        return "green";
    }
}
