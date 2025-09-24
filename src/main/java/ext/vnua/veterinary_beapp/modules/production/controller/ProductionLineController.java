package ext.vnua.veterinary_beapp.modules.production.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.production.dto.ProductionLineDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionLine.CreateProductionLineRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionLine.GetProductionLineRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionLine.UpdateProductionLineRequest;
import ext.vnua.veterinary_beapp.modules.production.mapper.ProductionLineMapper;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionLine;
import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionLineQuery;
import ext.vnua.veterinary_beapp.modules.production.services.ProductionLineService;
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
@RequestMapping("/production-lines")
@RequiredArgsConstructor
public class ProductionLineController {

    private ProductionLineMapper productionLineMapper;

    private final ProductionLineService lineService;

    // =================== GET ===================

    @GetMapping
    @ApiOperation(value = "Lọc và phân trang danh sách dây chuyền")
    public ResponseEntity<?> getAllLines(@Valid @ModelAttribute GetProductionLineRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getStart(), request.getLimit());
        Page<ProductionLine> page = lineService.getAllLines(request, pageRequest);

        return BaseResponse.successListData(
                page.getContent().stream().map(productionLineMapper::toDto).toList(),
                (int) page.getTotalElements()
        );
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Lấy dây chuyền theo ID")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        ProductionLineDto dto = lineService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/code/{lineCode}")
    @ApiOperation(value = "Lấy dây chuyền theo mã")
    public ResponseEntity<?> getByCode(@PathVariable String lineCode) {
        ProductionLineDto dto = lineService.getByCode(lineCode);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/active")
    @ApiOperation(value = "Danh sách dây chuyền đang hoạt động")
    public ResponseEntity<?> getActiveLines() {
        List<ProductionLineDto> lines = lineService.findActiveLines();
        return ResponseEntity.ok(lines);
    }

    @GetMapping("/status/{status}")
    @ApiOperation(value = "Danh sách dây chuyền theo trạng thái")
    public ResponseEntity<?> getByStatus(@PathVariable String status) {
        List<ProductionLineDto> lines = lineService.findByStatus(status);
        return ResponseEntity.ok(lines);
    }

    @GetMapping("/count/{status}")
    @ApiOperation(value = "Đếm số dây chuyền theo trạng thái")
    public ResponseEntity<?> countByStatus(@PathVariable String status) {
        long count = lineService.countByStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/search")
    @ApiOperation(value = "Tìm kiếm dây chuyền theo tên")
    public ResponseEntity<?> searchByName(@RequestParam String name) {
        List<ProductionLineDto> lines = lineService.searchByName(name);
        return ResponseEntity.ok(lines);
    }

    // =================== POST ===================

    @PostMapping
    @ApiOperation(value = "Tạo mới dây chuyền")
    public ResponseEntity<?> create(@Valid @RequestBody CreateProductionLineRequest request) {
        ProductionLineDto dto = lineService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // =================== PUT ===================

    @PutMapping
    @ApiOperation(value = "Cập nhật dây chuyền")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateProductionLineRequest request) {
        ProductionLineDto dto = lineService.update(request);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/toggle-status")
    @ApiOperation(value = "Chuyển đổi trạng thái dây chuyền")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        lineService.toggleStatus(id);
        return ResponseEntity.ok("Chuyển trạng thái dây chuyền thành công ID " + id);
    }

    // =================== DELETE ===================

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Xóa dây chuyền theo ID")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        lineService.delete(id);
        return ResponseEntity.ok("Xóa dây chuyền thành công ID " + id);
    }

    @DeleteMapping("/bulk")
    @ApiOperation(value = "Xóa nhiều dây chuyền theo danh sách ID")
    public ResponseEntity<?> deleteAll(@RequestBody List<Long> ids) {
        List<ProductionLineDto> deleted = lineService.deleteAll(ids);
        return ResponseEntity.ok(deleted);
    }
}
