package ext.vnua.veterinary_beapp.modules.production.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderMaterialDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.UpdateProductionOrderMaterialRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.materials.CreateProductionOrderMaterialRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.materials.GetProductionOrderMaterialRequest;
import ext.vnua.veterinary_beapp.modules.production.services.ProductionOrderMaterialService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/production-order-materials")
@RequiredArgsConstructor
public class ProductionOrderMaterialController {

    private final ProductionOrderMaterialService materialService;

    // =================== GET ===================

    @GetMapping
    @ApiOperation(value = "Tìm kiếm & phân trang chi tiết nguyên liệu lệnh sản xuất")
    public ResponseEntity<?> getAllMaterials(@Valid @ModelAttribute GetProductionOrderMaterialRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getStart(), request.getLimit());
        Page<ProductionOrderMaterialDto> page = materialService.searchMaterials(request, pageRequest);

        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Lấy chi tiết nguyên liệu theo ID")
    public ResponseEntity<?> getMaterialById(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    @ApiOperation(value = "Lấy tất cả chi tiết nguyên liệu theo lệnh sản xuất")
    public ResponseEntity<?> getMaterialsByOrder(@PathVariable Long orderId) {
        List<ProductionOrderMaterialDto> list = materialService.getByOrder(orderId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/order/{orderId}/status/{status}")
    @ApiOperation(value = "Lấy chi tiết nguyên liệu theo lệnh sản xuất và trạng thái")
    public ResponseEntity<?> getMaterialsByOrderAndStatus(@PathVariable Long orderId, @PathVariable String status) {
        return ResponseEntity.ok(materialService.getByOrderAndStatus(orderId, status));
    }

    // =================== POST ===================

    @PostMapping
    @ApiOperation(value = "Thêm nguyên liệu vào lệnh sản xuất")
    public ResponseEntity<?> createMaterial(@Valid @RequestBody CreateProductionOrderMaterialRequest request) {
        ProductionOrderMaterialDto dto = materialService.createMaterial(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/{orderId}/issue")
    @ApiOperation(value = "Cấp phát nhiều nguyên liệu cho lệnh sản xuất")
    public ResponseEntity<?> issueMaterials(@PathVariable Long orderId, @RequestBody List<Long> materialIds) {
        materialService.issueMaterials(orderId, materialIds);
        return ResponseEntity.ok("Cấp phát thành công " + materialIds.size() + " nguyên liệu");
    }

    // =================== PUT ===================

    @PutMapping
    @ApiOperation(value = "Cập nhật chi tiết nguyên liệu")
    public ResponseEntity<?> updateMaterial(@Valid @RequestBody UpdateProductionOrderMaterialRequest request) {
        return ResponseEntity.ok(materialService.updateMaterial(request));
    }

    @PutMapping("/{id}/status")
    @ApiOperation(value = "Cập nhật trạng thái nguyên liệu")
    public ResponseEntity<?> updateMaterialStatus(@PathVariable Long id, @RequestParam String status) {
        materialService.updateMaterialStatus(id, status);
        return ResponseEntity.ok("Cập nhật trạng thái thành công cho nguyên liệu ID " + id);
    }

    @PutMapping("/bulk/status")
    @ApiOperation(value = "Cập nhật trạng thái nhiều nguyên liệu")
    public ResponseEntity<?> bulkUpdateMaterialStatus(@RequestBody List<Long> ids, @RequestParam String status) {
        materialService.bulkUpdateMaterialStatus(ids, status);
        return ResponseEntity.ok("Cập nhật trạng thái thành công cho " + ids.size() + " nguyên liệu");
    }

    // =================== DELETE ===================

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Xóa chi tiết nguyên liệu")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.ok("Xóa chi tiết nguyên liệu thành công ID " + id);
    }
}
