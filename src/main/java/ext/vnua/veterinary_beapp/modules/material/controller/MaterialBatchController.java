package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchDto;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchItemDto;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchItemActiveIngredientDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateMaterialBatchContainerRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateMaterialBatchItemRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateMaterialBatchRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.GetMaterialBatchRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.UpdateMaterialBatchRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateTestResultRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.UpdateTestResultRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.response.MaterialBatchDetailDTO;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialBatchService;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialBatchItemService;
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

@RestController
@RequestMapping("/material-batches")
@RequiredArgsConstructor
public class MaterialBatchController {

    private final MaterialBatchService materialBatchService;
    private final MaterialBatchItemService materialBatchItemService;

    @GetMapping
    @ApiOperation(value = "Lấy danh sách lô vật liệu có phân trang và lọc")
    public ResponseEntity<?> getAllMaterialBatches(
            @Valid @ModelAttribute GetMaterialBatchRequest request) {

        Page<MaterialBatchDto> page = materialBatchService.getAllMaterialBatch(request, PageRequest.of(request.getStart(), request.getLimit()));

        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
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
    @ApiOperation(value = "Tạo mới lô vật liệu (deprecated - chỉ tương thích cũ)")
    public ResponseEntity<?> createMaterialBatch(
            @Valid @RequestBody CreateMaterialBatchRequest request) {

        MaterialBatchDto materialBatchDto = materialBatchService.createMaterialBatch(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(materialBatchDto);
    }

    @PostMapping("/container")
    @ApiOperation(value = "Tạo mới lô container (chỉ tạo container, thêm items sau)")
    public ResponseEntity<?> createMaterialBatchContainer(
            @Valid @RequestBody CreateMaterialBatchContainerRequest request) {

        MaterialBatchDto materialBatchDto = materialBatchService.createMaterialBatchContainer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(materialBatchDto);
    }

    @PostMapping("/{batchId}/items")
    @ApiOperation(value = "Thêm vật liệu (item) vào lô container")
    public ResponseEntity<?> addItemToBatch(
            @PathVariable Long batchId,
            @Valid @RequestBody CreateMaterialBatchItemRequest request) {

        MaterialBatchDto updatedBatch = materialBatchService.addItemToBatch(batchId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(updatedBatch);
    }

    @GetMapping("/{batchId}/items")
    @ApiOperation(value = "Lấy danh sách items trong lô")
    public ResponseEntity<?> getBatchItems(@PathVariable Long batchId) {
        List<MaterialBatchItemDto> items = materialBatchItemService.getItemsByBatchId(batchId);
        return BaseResponse.successListData(items, items.size());
    }

    @GetMapping("/items/{itemId}")
    @ApiOperation(value = "Lấy thông tin chi tiết một item")
    public ResponseEntity<?> getItemById(@PathVariable Long itemId) {
        MaterialBatchItemDto item = materialBatchItemService.getItemById(itemId);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/items/{itemId}/active-ingredients")
    @ApiOperation(value = "Lấy danh sách hoạt chất của item")
    public ResponseEntity<?> getItemActiveIngredients(@PathVariable Long itemId) {
        List<MaterialBatchItemActiveIngredientDto> ingredients = 
            materialBatchItemService.getActiveIngredientsByItemId(itemId);
        return BaseResponse.successListData(ingredients, ingredients.size());
    }

    @GetMapping("/active-ingredients/{ingredientId}")
    @ApiOperation(value = "Lấy thông tin chi tiết một hoạt chất")
    public ResponseEntity<?> getActiveIngredientById(@PathVariable Long ingredientId) {
        MaterialBatchItemActiveIngredientDto ingredient = 
            materialBatchItemService.getActiveIngredientById(ingredientId);
        return ResponseEntity.ok(ingredient);
    }

    @PostMapping("/test-results")
    @ApiOperation(value = "Tạo mới kết quả kiểm nghiệm (COA + KQPT)")
    public ResponseEntity<?> createTestResult(
            @Valid @RequestBody CreateTestResultRequest request) {
        MaterialBatchItemActiveIngredientDto result = 
            materialBatchItemService.createTestResult(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/test-results/{id}")
    @ApiOperation(value = "Cập nhật kết quả kiểm nghiệm")
    public ResponseEntity<?> updateTestResult(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTestResultRequest request) {
        MaterialBatchItemActiveIngredientDto result = 
            materialBatchItemService.updateTestResult(id, request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/test-results/{id}")
    @ApiOperation(value = "Xóa kết quả kiểm nghiệm")
    public ResponseEntity<?> deleteTestResult(@PathVariable Long id) {
        materialBatchItemService.deleteTestResult(id);
        return ResponseEntity.ok("Đã xóa kết quả kiểm nghiệm với ID: " + id);
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

    // ===== DEPRECATED: Active Ingredients Management =====
    // These endpoints are deprecated. Use MaterialBatchItem endpoints instead.
    // Active ingredients are now managed through MaterialBatchItemActiveIngredient
    
    /*
    @PostMapping("/{id}/active-ingredients")
    @ApiOperation(value = "Thêm hoạt chất vào lô vật liệu")
    public ResponseEntity<?> addActiveIngredient(
            @PathVariable Long id,
            @Valid @RequestBody MaterialBatchActiveIngredientRequest request) {
        MaterialBatchActiveIngredientDTO result = activeIngredientService.addActiveIngredientToBatch(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{id}/active-ingredients/batch")
    @ApiOperation(value = "Thêm nhiều hoạt chất vào lô vật liệu")
    public ResponseEntity<?> addMultipleActiveIngredients(
            @PathVariable Long id,
            @Valid @RequestBody List<MaterialBatchActiveIngredientRequest> requests) {
        List<MaterialBatchActiveIngredientDTO> result = activeIngredientService.addMultipleActiveIngredients(id, requests);
        return BaseResponse.successListData(result, result.size());
    }

    @PutMapping("/active-ingredients/{ingredientId}")
    @ApiOperation(value = "Cập nhật thông tin hoạt chất trong lô")
    public ResponseEntity<?> updateActiveIngredient(
            @PathVariable Long ingredientId,
            @Valid @RequestBody MaterialBatchActiveIngredientRequest request) {
        MaterialBatchActiveIngredientDTO result = activeIngredientService.updateActiveIngredient(ingredientId, request);
        return BaseResponse.successData(result);
    }

    @DeleteMapping("/active-ingredients/{ingredientId}")
    @ApiOperation(value = "Xóa hoạt chất khỏi lô vật liệu")
    public ResponseEntity<?> deleteActiveIngredient(@PathVariable Long ingredientId) {
        activeIngredientService.deleteActiveIngredient(ingredientId);
        return ResponseEntity.ok().build();
    }
    */
}
