package ext.vnua.veterinary_beapp.modules.product.controller;


import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductBatchDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.CompleteBatchRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.GetProductBatchRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.IssueBatchRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.SimulateConsumptionRequest;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductBatchMapper;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBatch;
import ext.vnua.veterinary_beapp.modules.product.servies.ProductBatchService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
}
