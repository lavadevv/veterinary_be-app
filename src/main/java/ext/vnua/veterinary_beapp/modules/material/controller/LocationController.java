package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.LocationDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.location.CreateLocationRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.location.GetLocationRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.location.UpdateLocationRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.LocationMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.service.LocationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    private final LocationMapper locationMapper;

    @ApiOperation(value = "Lấy tất cả vị trí với phân trang và bộ lọc")
    @GetMapping("")
    public ResponseEntity<?> getAllLocation(@Valid @ModelAttribute GetLocationRequest request) {
        Page<Location> page = locationService.getAllLocation(request, PageRequest.of(request.getStart(), request.getLimit()));

        return BaseResponse.successListData(page.getContent().stream()
                .map(locationMapper::toLocationDto)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @ApiOperation(value = "Lấy thông tin chi tiết của một vị trí theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getLocationById(@PathVariable("id") Long id) {
        LocationDto location = locationService.selectLocationById(id);
        return ResponseEntity.ok(location);
    }

    @ApiOperation(value = "Lấy thông tin chi tiết của một vị trí theo mã vị trí")
    @GetMapping("/code/{locationCode}")
    public ResponseEntity<?> getLocationByCode(
            @ApiParam(value = "Mã vị trí", required = true)
            @PathVariable("locationCode") String locationCode) {
        LocationDto location = locationService.selectLocationByCode(locationCode);
        return ResponseEntity.ok(location);
    }

    @ApiOperation(value = "Lấy danh sách vị trí theo kho")
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<?> getLocationsByWarehouse(
            @ApiParam(value = "ID của kho", required = true)
            @PathVariable("warehouseId") Long warehouseId) {
        List<LocationDto> locations = locationService.selectLocationsByWarehouse(warehouseId);
        return BaseResponse.successListData(locations, locations.size());
    }

    @ApiOperation(value = "Lấy danh sách vị trí khả dụng theo kho")
    @GetMapping("/warehouse/{warehouseId}/available")
    public ResponseEntity<?> getAvailableLocationsByWarehouse(
            @ApiParam(value = "ID của kho", required = true)
            @PathVariable("warehouseId") Long warehouseId) {
        List<LocationDto> locations = locationService.getAvailableLocationsByWarehouse(warehouseId);
        return BaseResponse.successListData(locations, locations.size());
    }

    @ApiOperation(value = "Tạo vị trí mới")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createLocation(@Valid @RequestBody CreateLocationRequest request) {
        LocationDto newLocation = locationService.createLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newLocation);
    }

    @ApiOperation(value = "Cập nhật thông tin vị trí")
    @PutMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateLocation(@Valid @RequestBody UpdateLocationRequest request) {
        LocationDto updatedLocation = locationService.updateLocation(request);
        return ResponseEntity.ok(updatedLocation);
    }

    @ApiOperation(value = "Cập nhật sức chứa hiện tại của vị trí")
    @PutMapping("/{id}/capacity")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<?> updateCurrentCapacity(
            @ApiParam(value = "ID của vị trí", required = true)
            @PathVariable("id") Long id,
            @ApiParam(value = "Sức chứa mới", required = true)
            @RequestParam("capacity") Double capacity) {
        locationService.updateCurrentCapacity(id, capacity);
        return ResponseEntity.ok("Cập nhật sức chứa thành công");
    }

    @ApiOperation(value = "Chuyển đổi trạng thái khả dụng của vị trí")
    @PutMapping("/{id}/toggle-availability")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<?> toggleAvailability(
            @ApiParam(value = "ID của vị trí", required = true)
            @PathVariable("id") Long id) {
        locationService.toggleAvailability(id);
        return ResponseEntity.ok("Chuyển đổi trạng thái thành công");
    }

    @ApiOperation(value = "Xóa một vị trí")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteLocation(
            @ApiParam(value = "ID của vị trí", required = true)
            @PathVariable("id") Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.ok("Xóa vị trí thành công");
    }

    @ApiOperation(value = "Xóa nhiều vị trí theo danh sách ID")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteMultipleLocations(
            @ApiParam(value = "Danh sách ID các vị trí cần xóa", required = true)
            @RequestBody List<Long> ids) {
        List<LocationDto> deletedLocations = locationService.deleteAllIdLocations(ids);
        return BaseResponse.successListData(deletedLocations, deletedLocations.size());
    }
}