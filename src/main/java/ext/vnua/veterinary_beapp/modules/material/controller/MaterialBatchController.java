package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateMaterialBatchRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.GetMaterialBatchRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.UpdateMaterialBatchRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.MaterialBatchMapper;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialBatchService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/material-batches")
@RequiredArgsConstructor
public class MaterialBatchController {

    private final MaterialBatchService materialBatchService;

    private final MaterialBatchMapper materialBathMapper;

    @GetMapping
    @ApiOperation(value = "Lấy danh sách lô vật liệu có phân trang và lọc")
    public ResponseEntity<?> getAllMaterialBatches(
            @Valid @ModelAttribute GetMaterialBatchRequest request) {

        Page<MaterialBatch> page = materialBatchService.getAllMaterialBatch(request, PageRequest.of(request.getStart(), request.getLimit()));

        return BaseResponse.successListData(page.getContent().stream()
                .map(materialBathMapper::toMaterialBatchDto)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Lấy thông tin lô vật liệu theo ID")
    public ResponseEntity<?> getMaterialBatchById(
            @PathVariable Long id) {

        MaterialBatchDto materialBatchDto = materialBatchService.selectMaterialBatchById(id);

        return ResponseEntity.ok(materialBatchDto);
    }

    @GetMapping("/batch-number/{batchNumber}")
    @ApiOperation(value = "Lấy thông tin lô vật liệu theo số lô")
    public ResponseEntity<?> getMaterialBatchByBatchNumber(
            @PathVariable String batchNumber) {
        MaterialBatchDto materialBatchDto = materialBatchService.selectMaterialBatchByBatchNumber(batchNumber);
        return ResponseEntity.ok(materialBatchDto);
    }

    @GetMapping("/internal-code/{internalCode}")
    @ApiOperation(value = "Lấy thông tin lô vật liệu theo mã lô nội bộ")
    public ResponseEntity<?> getMaterialBatchByInternalCode(
            @PathVariable String internalCode) {
        MaterialBatchDto materialBatchDto = materialBatchService.selectMaterialBatchByInternalCode(internalCode);
        return ResponseEntity.ok(materialBatchDto);
    }

    @GetMapping("/by-material/{materialId}")
    @ApiOperation(value = "Lấy danh sách lô vật liệu theo vật liệu")
    public ResponseEntity<?> getMaterialBatchesByMaterial(
            @PathVariable Long materialId) {
        List<MaterialBatchDto> materialBatches = materialBatchService.selectMaterialBatchesByMaterial(materialId);
        return ResponseEntity.ok(materialBatches);
    }

    @GetMapping("/by-location/{locationId}")
    @ApiOperation(value = "Lấy danh sách lô vật liệu theo vị trí")
    public ResponseEntity<?> getMaterialBatchesByLocation(
            @PathVariable Long locationId) {
        List<MaterialBatchDto> materialBatches = materialBatchService.selectMaterialBatchesByLocation(locationId);
        return ResponseEntity.ok(materialBatches);
    }

    @PostMapping
    @ApiOperation(value = "Tạo mới lô vật liệu")
    public ResponseEntity<?> createMaterialBatch(
            @Valid @RequestBody CreateMaterialBatchRequest request) {

        MaterialBatchDto materialBatchDto = materialBatchService.createMaterialBatch(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(materialBatchDto);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Cập nhật thông tin lô vật liệu")
    public ResponseEntity<?> updateMaterialBatch(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMaterialBatchRequest request) {

        request.setId(id);
        MaterialBatchDto materialBatchDto = materialBatchService.updateMaterialBatch(request);

        return ResponseEntity.ok(materialBatchDto);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Xóa lô vật liệu")
    public ResponseEntity<?> deleteMaterialBatch(
            @PathVariable Long id) {
        materialBatchService.deleteMaterialBatch(id);
        return ResponseEntity.ok("Đã xóa lô vật liệu với ID: " + id);
    }

    @DeleteMapping("/batch")
    @ApiOperation(value = "Xóa nhiều lô vật liệu")
    public ResponseEntity<?> deleteMultipleMaterialBatches(
            @RequestBody List<Long> ids) {

        List<MaterialBatchDto> deletedBatches = materialBatchService.deleteAllIdMaterialBatches(ids);
        return ResponseEntity.ok(deletedBatches);
    }

    @PutMapping("/{id}/quantity")
    @ApiOperation(value = "Cập nhật số lượng lô vật liệu")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long id,
            @RequestParam BigDecimal quantity) {

        materialBatchService.updateQuantity(id, quantity);
        return ResponseEntity.ok("Đã cập nhật số lượng cho lô vật liệu ID: " + id);
    }

    @PutMapping("/{id}/test-status")
    @ApiOperation(value = "Cập nhật trạng thái kiểm nghiệm")
    public ResponseEntity<?> updateTestStatus(
            @PathVariable Long id,
            @RequestParam String testStatus) {

        materialBatchService.updateTestStatus(id, testStatus);
        return ResponseEntity.ok("Đã cập nhật trạng thái kiểm nghiệm cho lô vật liệu ID: " + id);
    }

    @PutMapping("/{id}/usage-status")
    @ApiOperation(value = "Cập nhật trạng thái sử dụng")
    public ResponseEntity<?> updateUsageStatus(
            @PathVariable Long id,
            @RequestParam String usageStatus) {

        materialBatchService.updateUsageStatus(id, usageStatus);

        return ResponseEntity.ok("Đã cập nhật trạng thái sử dụng cho lô vật liệu ID: " + id);
    }

    @PutMapping("/{id}/move-location")
    @ApiOperation(value = "Di chuyển lô vật liệu đến vị trí mới")
    public ResponseEntity<?> moveToLocation(
            @PathVariable Long id,
            @RequestParam(required = false) Long newLocationId) {

        materialBatchService.moveToLocation(id, newLocationId);

        return ResponseEntity.ok("Đã di chuyển lô vật liệu ID: " + id + " đến vị trí mới");
    }

    @GetMapping("/expired")
    @ApiOperation(value = "Lấy danh sách lô vật liệu đã hết hạn")
    public ResponseEntity<?> getExpiredBatches() {

        List<MaterialBatchDto> expiredBatches = materialBatchService.getExpiredBatches();
        return ResponseEntity.ok(expiredBatches);
    }

    @GetMapping("/near-expiry")
    @ApiOperation(value = "Lấy danh sách lô vật liệu sắp hết hạn")
    public ResponseEntity<?> getBatchesNearExpiry() {

        List<MaterialBatchDto> nearExpiryBatches = materialBatchService.getBatchesNearExpiry();

        return ResponseEntity.ok(nearExpiryBatches);
    }

    @GetMapping("/usable")
    @ApiOperation(value = "Lấy danh sách lô vật liệu có thể sử dụng")
    public ResponseEntity<?> getUsableBatches() {

        List<MaterialBatchDto> usableBatches = materialBatchService.getUsableBatches();

        return ResponseEntity.ok(usableBatches);
    }

    @GetMapping("/total-quantity/{materialId}")
    @ApiOperation(value = "Lấy tổng số lượng theo vật liệu")
    public ResponseEntity<?> getTotalQuantityByMaterial(
            @PathVariable Long materialId) {

        BigDecimal totalQuantity = materialBatchService.getTotalQuantityByMaterial(materialId);

        return ResponseEntity.ok(totalQuantity);
    }

    @GetMapping("/oldest-usable/{materialId}")
    @ApiOperation(value = "Lấy danh sách lô cũ nhất có thể sử dụng theo FIFO")
    public ResponseEntity<?> getOldestUsableBatches(
            @PathVariable Long materialId) {

        List<MaterialBatchDto> oldestBatches = materialBatchService.getOldestUsableBatches(materialId);

        return ResponseEntity.ok(oldestBatches);
    }
}