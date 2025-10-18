// ext/vnua/veterinary_beapp/modules/pcost/controller/ProductionCostSheetController.java
package ext.vnua.veterinary_beapp.modules.pcost.controller;

import ext.vnua.veterinary_beapp.modules.pcost.dto.GetProductionCostSheets;
import ext.vnua.veterinary_beapp.modules.pcost.dto.ProductionCostSheetDto;
import ext.vnua.veterinary_beapp.modules.pcost.dto.UpsertProductionCostSheetRequest;
import ext.vnua.veterinary_beapp.modules.pcost.service.ProductionCostSheetService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/production-cost-sheets")
@RequiredArgsConstructor
public class ProductionCostSheetController {

    private final ProductionCostSheetService service;

    @PostMapping
    @ApiOperation("Tạo bảng chi phí sản xuất")
    public ResponseEntity<ProductionCostSheetDto> create(@Valid @RequestBody UpsertProductionCostSheetRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @ApiOperation("Cập nhật bảng chi phí sản xuất")
    public ResponseEntity<ProductionCostSheetDto> update(@PathVariable Long id,
                                                         @Valid @RequestBody UpsertProductionCostSheetRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @GetMapping("/{id}")
    @ApiOperation("Xem chi tiết bảng chi phí sản xuất")
    public ResponseEntity<ProductionCostSheetDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/by-code/{code}")
    @ApiOperation("Lấy bảng chi phí theo mã sheetCode")
    public ResponseEntity<ProductionCostSheetDto> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.getByCode(code));
    }

    @GetMapping("/by-product/{productId}")
    @ApiOperation("Danh sách bảng chi phí theo productId")
    public ResponseEntity<List<ProductionCostSheetDto>> listByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(service.listByProduct(productId));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("Xoá bảng chi phí sản xuất")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Đã xoá");
    }

    @GetMapping("/search")
    @ApiOperation("Tìm kiếm/paging Production Cost Sheet (start/limit + bộ lọc)")
    public ResponseEntity<?> search(@Valid GetProductionCostSheets req) {
        // Trả raw { total, items } hoặc bạn có thể wrap BaseResponse tuỳ global handler
        return ResponseEntity.ok(service.search(req));
    }
}
