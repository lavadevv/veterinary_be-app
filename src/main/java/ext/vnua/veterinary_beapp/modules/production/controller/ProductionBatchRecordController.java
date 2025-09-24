package ext.vnua.veterinary_beapp.modules.production.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.production.dto.ProductionBatchRecordDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionBatchRecord.CreateProductionBatchRecordRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionBatchRecord.UpdateProductionBatchRecordRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionBatchRecord.GetProductionBatchRecordRequest;
import ext.vnua.veterinary_beapp.modules.production.services.ProductionBatchRecordService;
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
@RequestMapping("/production-batch-records")
@RequiredArgsConstructor
public class ProductionBatchRecordController {

    private final ProductionBatchRecordService recordService;

    // =================== GET ===================

    @GetMapping
    @ApiOperation(value = "Tìm kiếm và phân trang hồ sơ lô sản xuất")
    public ResponseEntity<?> getAll(@Valid @ModelAttribute GetProductionBatchRecordRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getStart(), request.getLimit());
        Page<ProductionBatchRecordDto> page = recordService.getAll(request, pageRequest);

        return BaseResponse.successListData(
                page.getContent(),
                (int) page.getTotalElements()
        );
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Lấy hồ sơ lô sản xuất theo ID")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        ProductionBatchRecordDto dto = recordService.getById(id);
        return ResponseEntity.ok(dto);
    }

    // =================== POST ===================

    @PostMapping
    @ApiOperation(value = "Tạo mới hồ sơ lô sản xuất")
    public ResponseEntity<?> create(@Valid @RequestBody CreateProductionBatchRecordRequest request) {
        ProductionBatchRecordDto dto = recordService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // =================== PUT ===================

    @PutMapping
    @ApiOperation(value = "Cập nhật hồ sơ lô sản xuất")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateProductionBatchRecordRequest request) {
        ProductionBatchRecordDto dto = recordService.update(request);
        return ResponseEntity.ok(dto);
    }

    // =================== DELETE ===================

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Xóa hồ sơ lô sản xuất theo ID")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        recordService.delete(id);
        return ResponseEntity.ok("Xóa hồ sơ lô sản xuất thành công với ID: " + id);
    }

    @DeleteMapping("/bulk")
    @ApiOperation(value = "Xóa nhiều hồ sơ lô sản xuất theo danh sách ID")
    public ResponseEntity<?> deleteAll(@RequestBody List<Long> ids) {
        List<ProductionBatchRecordDto> deleted = recordService.deleteAll(ids);
        return ResponseEntity.ok(deleted);
    }
}
