package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.WarehouseDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse.CreateWarehouseRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse.GetWarehouseRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse.UpdateWarehouseRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.WarehouseMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Warehouse;
import ext.vnua.veterinary_beapp.modules.material.service.WarehouseService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;

    @GetMapping
    @ApiOperation(value = "Lấy tất cả nhà kho")
    public ResponseEntity<?> getAllWarehouses(@Valid @ModelAttribute GetWarehouseRequest request) {

        Page<Warehouse> page = warehouseService.getAllWarehouse(request, PageRequest.of(request.getStart(), request.getLimit()));

        return BaseResponse.successListData(page.getContent().stream()
                .map(warehouseMapper::toWarehouseDto)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @GetMapping("/all")
    @ApiOperation(value = "Lấy tất cả nhà kho không phân trang")
    public ResponseEntity<?> getWarehouses() {
        List<WarehouseDto> dtos = warehouseService.getAllWarehouses();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Lấy warehouse theo id")
    public ResponseEntity<?> getWarehouseById(@PathVariable Long id) {
        WarehouseDto warehouseDto = warehouseService.selectWarehouseById(id);
        return ResponseEntity.ok(warehouseDto);
    }

    @GetMapping("/code/{warehouseCode}")
    @ApiOperation(value = "Lấy nhà kho theo code")
    public ResponseEntity<?> getWarehouseByCode(@PathVariable String warehouseCode) {
        WarehouseDto warehouseDto = warehouseService.selectWarehouseByCode(warehouseCode);
        return ResponseEntity.ok(warehouseDto);
    }

    @GetMapping("/active")
    @ApiOperation(value = "Lấy tất cả warehouses đang hoạt động")
    public ResponseEntity<?> getAllActiveWarehouses() {
        List<WarehouseDto> warehouseDtos = warehouseService.selectAllActiveWarehouses();
        return ResponseEntity.ok(warehouseDtos);
    }

    @GetMapping("/type/{warehouseType}")
    @ApiOperation(value = "Get warehouses by type")
    public ResponseEntity<?> getWarehousesByType(@PathVariable String warehouseType) {
        List<WarehouseDto> warehouseDtos = warehouseService.getWarehousesByType(warehouseType);
        return ResponseEntity.ok(warehouseDtos);
    }

    @PostMapping
    @ApiOperation(value = "Tạo mới warehouse")
    public ResponseEntity<?> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request) {
        WarehouseDto warehouseDto = warehouseService.createWarehouse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseDto);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Cập nhật warehouse")
    public ResponseEntity<?> updateWarehouse(@PathVariable Long id,
            @Valid @RequestBody UpdateWarehouseRequest request) {
        request.setId(id);
        WarehouseDto warehouseDto = warehouseService.updateWarehouse(request);
        return ResponseEntity.ok(warehouseDto);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Xoá warehouse")
    public ResponseEntity<?> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok("Xóa kho thành công");
    }

    @DeleteMapping("/batch")
    @ApiOperation(value = "Xoá nhiều warehouses")
    public ResponseEntity<?> deleteWarehouses(
            @RequestBody List<Long> ids) {
        List<WarehouseDto> deletedWarehouses = warehouseService.deleteAllIdWarehouses(ids);
        return ResponseEntity.ok(deletedWarehouses);
    }

    @PatchMapping("/{id}/toggle-status")
    @ApiOperation(value = "Toggle warehouse active status")
    public ResponseEntity<?> toggleWarehouseStatus(@PathVariable Long id) {
        warehouseService.toggleActiveStatus(id);
        return ResponseEntity.ok("Đã thay đổi trạng thái kho");
    }

}
