package ext.vnua.veterinary_beapp.modules.product.controller;


import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductBatchDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.*;
import ext.vnua.veterinary_beapp.modules.product.dto.response.productBatch.CalcBatchRes;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductBatchMapper;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBatch;
import ext.vnua.veterinary_beapp.modules.product.services.ProductBatchService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/product-batch")
@RequiredArgsConstructor
public class ProductBatchController {
    private final ProductBatchService batchService;
    private final ProductBatchMapper batchMapper;

    @ApiOperation(value = "Lấy danh sách lô thành phẩm (filter + paging)")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER','PRODUCTION_MANAGER','QC_STAFF','SALES_STAFF','ACCOUNTANT','VIEWER')")
    public ResponseEntity<?> list(@Valid @ModelAttribute GetProductBatchRequest req) {
        Page<ProductBatch> page = batchService.getAllBatches(req, PageRequest.of(req.getStart(), req.getLimit()));
        return BaseResponse.successListData(page.getContent().stream()
                .map(batchMapper::toDto).collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @ApiOperation(value = "Xem chi tiết lô theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER','PRODUCTION_MANAGER','QC_STAFF','SALES_STAFF','ACCOUNTANT','VIEWER')")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.getById(id));
    }

    @ApiOperation(value = "Mô phỏng tiêu hao NVL theo FIFO")
    @PostMapping("/simulate")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','WAREHOUSE_MANAGER')")
    public ResponseEntity<?> simulate(@Valid @RequestBody SimulateConsumptionRequest req) {
        return ResponseEntity.ok(batchService.simulateConsumption(req));
    }

    @ApiOperation(value = "Phát hành lệnh sản xuất (issue) + reserve NVL + sinh batch number")
    @PostMapping("/issue")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','WAREHOUSE_MANAGER')")
    public ResponseEntity<?> issue(@Valid @RequestBody IssueBatchRequest req) {
        ProductBatchDto dto = batchService.issueBatch(req);
        return ResponseEntity.ok(dto);
    }

    @ApiOperation(value = "Hoàn thành lô: trừ NVL, nhập kho TP, tính hiệu suất")
    @PutMapping("/complete")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF','WAREHOUSE_MANAGER')")
    public ResponseEntity<?> complete(@Valid @RequestBody CompleteBatchRequest req) {
        return ResponseEntity.ok(batchService.completeBatch(req));
    }

    @ApiOperation(value = "Đóng lô thành phẩm")
    @PutMapping("/{id}/close")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER')")
    public ResponseEntity<?> close(@PathVariable("id") Long id) {
        batchService.closeBatch(id);
        return ResponseEntity.ok("Đã đóng lô");
    }

    @ApiOperation(value = "Tính bảng tiêu hao & giá thành theo công thức (BE tính, FE chỉ hiển thị)")
    @PostMapping("/calc")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','WAREHOUSE_MANAGER','QC_STAFF','VIEWER')")
    public ResponseEntity<CalcBatchRes> calc(@Valid @RequestBody CalcBatchReq req) {
        return ResponseEntity.ok(batchService.calc(req));
    }

    @ApiOperation(value = "Tạo lô nháp từ công thức (chưa issue NVL)")
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','WAREHOUSE_MANAGER')")
    public ResponseEntity<ProductBatchDto> createDraft(@Valid @RequestBody CreateBatchReq req) {
        return ResponseEntity.ok(batchService.createDraft(req));
    }

    @ApiOperation(value = "Hủy lệnh sản xuất (unissue) – xả NVL đã reserve")
    @PutMapping("/cancel")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','WAREHOUSE_MANAGER')")
    public ResponseEntity<?> cancel(@Valid @RequestBody CancelBatchRequest req) {
        batchService.cancelBatch(req);
        return ResponseEntity.ok("Đã hủy lệnh & xả NVL dự trữ cho batch " + req.getBatchId());
    }
}
