package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.StockAlertDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.stockAlert.CreateStockAlertRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.stockAlert.GetStockAlertRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.stockAlert.ResolveAlertRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.stockAlert.UpdateStockAlertRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.StockAlertMapper;
import ext.vnua.veterinary_beapp.modules.material.model.StockAlert;
import ext.vnua.veterinary_beapp.modules.material.service.StockAlertService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stock-alerts")
@RequiredArgsConstructor
public class StockAlertController {

    private final StockAlertService stockAlertService;

    private final StockAlertMapper stockAlertMapper;

    @ApiOperation(value = "Lấy danh sách cảnh báo kho có phân trang và bộ lọc")
    @GetMapping
    public ResponseEntity<?> getAllStockAlerts(@Valid @ModelAttribute GetStockAlertRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getStart(), request.getLimit());
        Page<StockAlert> page = stockAlertService.getAllStockAlert(request, pageRequest);

        return BaseResponse.successListData(page.getContent().stream()
                .map(stockAlertMapper::toStockAlertDto)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @ApiOperation(value = "Lấy thông tin cảnh báo kho theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getStockAlertById(@PathVariable Long id) {
            StockAlertDto stockAlertDto = stockAlertService.selectStockAlertById(id);
            return ResponseEntity.ok(stockAlertDto);
    }

    @ApiOperation(value = "Lấy danh sách cảnh báo theo ID vật liệu")
    @GetMapping("/material/{materialId}")
    public ResponseEntity<?> getStockAlertsByMaterial(@PathVariable Long materialId) {
            List<StockAlertDto> stockAlertDtos = stockAlertService.selectStockAlertsByMaterial(materialId);
            return  ResponseEntity.ok(stockAlertDtos);
    }

    @ApiOperation(value = "Lấy danh sách tất cả cảnh báo chưa được giải quyết")
    @GetMapping("/unresolved")
    public ResponseEntity<?> getUnresolvedAlerts() {
            List<StockAlertDto> stockAlertDtos = stockAlertService.selectUnresolvedAlerts();
            return  ResponseEntity.ok(stockAlertDtos);
    }

    @ApiOperation(value = "Tạo một cảnh báo kho mới")
    @PostMapping
    public ResponseEntity<?> createStockAlert(@Valid @RequestBody CreateStockAlertRequest request) {
            StockAlertDto createdStockAlert = stockAlertService.createStockAlert(request);
            return ResponseEntity.ok(createdStockAlert);
    }

    @ApiOperation(value = "Cập nhật thông tin của một cảnh báo kho")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStockAlert(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateStockAlertRequest request) {
            request.setId(id);
            StockAlertDto updatedStockAlert = stockAlertService.updateStockAlert(request);
            return ResponseEntity.ok(updatedStockAlert);
    }

    @ApiOperation(value = "Đánh dấu cảnh báo là đã được giải quyết")
    @PutMapping("/resolve")
    public ResponseEntity<?> resolveAlert(@Valid @RequestBody ResolveAlertRequest request) {
            StockAlertDto resolvedAlert = stockAlertService.resolveAlert(
                    request.getAlertId(), request.getUserId(), request.getResolutionNotes());

            return ResponseEntity.ok(resolvedAlert);
    }

    @ApiOperation(value = "Xóa một cảnh báo kho theo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStockAlert(@PathVariable Long id) {
            stockAlertService.deleteStockAlert(id);

            return ResponseEntity.ok("Xóa cảnh báo kho thành công");
    }

    @ApiOperation(value = "Xóa danh sách cảnh báo kho theo danh sách ID")
    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteMultipleStockAlerts(@RequestBody List<Long> ids) {
            List<StockAlertDto> deletedAlerts = stockAlertService.deleteAllIdStockAlerts(ids);
            return ResponseEntity.ok(deletedAlerts);
    }

    @ApiOperation(value = "Đếm tổng số cảnh báo chưa được giải quyết")
    @GetMapping("/count/unresolved")
    public ResponseEntity<?> countUnresolvedAlerts() {
            Long count = stockAlertService.countUnresolvedAlerts();
            return ResponseEntity.ok(count);
    }

    @ApiOperation(value = "Đếm số cảnh báo chưa giải quyết theo loại cảnh báo")
    @GetMapping("/count/unresolved/{alertType}")
    public ResponseEntity<?> countUnresolvedAlertsByType(@PathVariable StockAlert.AlertType alertType) {
            Long count = stockAlertService.countUnresolvedAlertsByType(alertType);
            return ResponseEntity.ok(count);
    }

    @ApiOperation(value = "Lấy danh sách cảnh báo trong khoảng thời gian nhất định")
    @GetMapping("/date-range")
    public ResponseEntity<?> getAlertsByDateRange(
            @RequestParam LocalDateTime fromDate,
            @RequestParam LocalDateTime toDate) {
            List<StockAlertDto> stockAlertDtos = stockAlertService.getAlertsByDateRange(fromDate, toDate);
            return ResponseEntity.ok(stockAlertDtos);
    }

    // Business endpoints for creating specific types of alerts
    @ApiOperation(value = "Tạo cảnh báo khi tồn kho thấp hơn ngưỡng tối thiểu")
    @PostMapping("/low-stock")
    public ResponseEntity<?> createLowStockAlert(
            @RequestParam Long materialId,
            @RequestParam Double currentStock,
            @RequestParam Double minThreshold) {
            stockAlertService.createLowStockAlert(materialId, currentStock, minThreshold);

            return ResponseEntity.ok("Tạo cảnh báo tồn kho thấp thành công");
    }

    @ApiOperation(value = "Tạo cảnh báo cho lô hàng đã hết hạn")
    @PostMapping("/expired")
    public ResponseEntity<?> createExpiryAlert(
            @RequestParam Long materialBatchId,
            @RequestParam LocalDateTime expiryDate) {
            stockAlertService.createExpiryAlert(materialBatchId, expiryDate);

            return ResponseEntity.ok("Tạo cảnh báo hết hạn thành công");
    }

    @ApiOperation(value = "Tạo cảnh báo cho lô hàng sắp hết hạn")
    @PostMapping("/near-expiry")
    public ResponseEntity<?> createNearExpiryAlert(
            @RequestParam Long materialBatchId,
            @RequestParam LocalDateTime expiryDate,
            @RequestParam int daysBeforeExpiry) {
            stockAlertService.createNearExpiryAlert(materialBatchId, expiryDate, daysBeforeExpiry);
            return ResponseEntity.ok("Tạo cảnh báo lô hàng sắp hết hạn thành công");
    }

    @ApiOperation(value = "Tạo cảnh báo khi tồn kho có giá trị âm")
    @PostMapping("/negative-stock")
    public ResponseEntity<?> createNegativeStockAlert(
            @RequestParam Long materialId,
            @RequestParam Double currentStock) {
            stockAlertService.createNegativeStockAlert(materialId, currentStock);
            return ResponseEntity.ok("Tạo cảnh báo khi tồn kho có giá trị âm thành công");
    }

    @ApiOperation(value = "Tạo cảnh báo cho lô hàng cần được cách ly")
    @PostMapping("/quarantine")
    public ResponseEntity<?> createQuarantineAlert(
            @RequestParam Long materialBatchId,
            @RequestParam String reason) {
            stockAlertService.createQuarantineAlert(materialBatchId, reason);

            return ResponseEntity.ok("Tạo cảnh báo cho lô hàng cần được cách ly thành công");
    }
}
