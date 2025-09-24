package ext.vnua.veterinary_beapp.modules.production.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderIssueDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionOrderIssue.CreateProductionOrderIssueRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionOrderIssue.UpdateProductionOrderIssueRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionOrderIssue.GetProductionOrderIssueRequest;
import ext.vnua.veterinary_beapp.modules.production.enums.IssueStatus;
import ext.vnua.veterinary_beapp.modules.production.enums.IssueType;
import ext.vnua.veterinary_beapp.modules.production.services.ProductionOrderIssueService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/production-order-issues")
@RequiredArgsConstructor
public class ProductionOrderIssueController {

    private final ProductionOrderIssueService issueService;

    // =================== GET ===================

    @GetMapping
    @ApiOperation(value = "Tìm kiếm và phân trang phiếu cấp phát")
    public ResponseEntity<?> getAll(@Valid @ModelAttribute GetProductionOrderIssueRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getStart(), request.getLimit());
        Page<ProductionOrderIssueDto> page = issueService.getAll(request, pageRequest);

        return BaseResponse.successListData(
                page.getContent(),
                (int) page.getTotalElements()
        );
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Lấy phiếu cấp phát theo ID")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        ProductionOrderIssueDto dto = issueService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/code/{issueCode}")
    @ApiOperation(value = "Lấy phiếu cấp phát theo mã")
    public ResponseEntity<?> getByCode(@PathVariable String issueCode) {
        ProductionOrderIssueDto dto = issueService.getByCode(issueCode);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/order/{orderId}")
    @ApiOperation(value = "Lấy danh sách phiếu cấp phát theo lệnh sản xuất")
    public ResponseEntity<?> getByOrder(@PathVariable Long orderId) {
        List<ProductionOrderIssueDto> list = issueService.getByOrder(orderId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/order/{orderId}/type/{issueType}")
    @ApiOperation(value = "Lấy danh sách phiếu cấp phát theo loại")
    public ResponseEntity<?> getByOrderAndType(@PathVariable Long orderId,
                                               @PathVariable IssueType issueType) {
        List<ProductionOrderIssueDto> list = issueService.getByOrderAndType(orderId, issueType);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/pending")
    @ApiOperation(value = "Lấy danh sách phiếu cấp phát đang chờ xử lý")
    public ResponseEntity<?> getPendingIssues() {
        return ResponseEntity.ok(issueService.getPendingIssues());
    }

    @GetMapping("/statistics")
    @ApiOperation(value = "Thống kê trạng thái phiếu cấp phát (toàn bộ hoặc theo lệnh)")
    public ResponseEntity<?> getStatusStatistics(@RequestParam(required = false) Long orderId) {
        Map<IssueStatus, Long> stats = issueService.getStatusStatistics(orderId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/order/{orderId}/active")
    @ApiOperation(value = "Kiểm tra lệnh sản xuất có phiếu cấp phát đang chờ xử lý không")
    public ResponseEntity<?> hasActiveIssues(@PathVariable Long orderId) {
        return ResponseEntity.ok(issueService.hasActiveIssues(orderId));
    }

    // =================== POST ===================

    @PostMapping
    @ApiOperation(value = "Tạo mới phiếu cấp phát")
    public ResponseEntity<?> create(@Valid @RequestBody CreateProductionOrderIssueRequest request) {
        ProductionOrderIssueDto dto = issueService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // =================== PUT ===================

    @PutMapping
    @ApiOperation(value = "Cập nhật phiếu cấp phát")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateProductionOrderIssueRequest request) {
        ProductionOrderIssueDto dto = issueService.update(request);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/approve")
    @ApiOperation(value = "Phê duyệt phiếu cấp phát")
    public ResponseEntity<?> approve(@PathVariable Long id,
                                     @RequestParam Long approverId) {
        return ResponseEntity.ok(issueService.approve(id, approverId));
    }

    @PutMapping("/{id}/cancel")
    @ApiOperation(value = "Hủy phiếu cấp phát")
    public ResponseEntity<?> cancel(@PathVariable Long id,
                                    @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(issueService.cancel(id, reason));
    }

    // =================== DELETE ===================

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Xóa phiếu cấp phát theo ID")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        issueService.delete(id);
        return ResponseEntity.ok("Xóa phiếu cấp phát thành công với ID: " + id);
    }

    @DeleteMapping("/bulk")
    @ApiOperation(value = "Xóa nhiều phiếu cấp phát")
    public ResponseEntity<?> deleteAll(@RequestBody List<Long> ids) {
        List<ProductionOrderIssueDto> deleted = issueService.deleteAll(ids);
        return ResponseEntity.ok(deleted);
    }
}
