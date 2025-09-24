package ext.vnua.veterinary_beapp.modules.production.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.*;
import ext.vnua.veterinary_beapp.modules.production.enums.ProductionOrderStatus;
import ext.vnua.veterinary_beapp.modules.production.mapper.ProductionOrderMapper;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionOrderQuery;
import ext.vnua.veterinary_beapp.modules.production.services.ProductionOrderService;
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
@RequestMapping("/production-orders")
@RequiredArgsConstructor
public class ProductionOrderController {

    private final ProductionOrderService orderService;
    private final ProductionOrderMapper orderMapper;

    // =================== GET ===================

    @GetMapping
    @ApiOperation(value = "Tìm kiếm và phân trang lệnh sản xuất")
    public ResponseEntity<?> getAllOrders(@Valid @ModelAttribute GetProductionOrderRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getStart(), request.getLimit());
        Page<ProductionOrder> page = orderService.searchOrders(request, pageRequest);

        return BaseResponse.successListData(
                page.getContent().stream().map(orderMapper::toDto).collect(Collectors.toList()),
                (int) page.getTotalElements()
        );
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Lấy lệnh sản xuất theo ID")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        ProductionOrderDto dto = orderService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/code/{orderCode}")
    @ApiOperation(value = "Lấy lệnh sản xuất theo mã lệnh")
    public ResponseEntity<?> getOrderByCode(@PathVariable String orderCode) {
        ProductionOrderDto dto = orderService.getByOrderCode(orderCode);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/product/{productId}")
    @ApiOperation(value = "Lấy danh sách lệnh sản xuất theo sản phẩm")
    public ResponseEntity<?> getOrdersByProduct(@PathVariable Long productId) {
        List<ProductionOrderDto> orders = orderService.getOrdersByProduct(productId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/line/{lineId}")
    @ApiOperation(value = "Lấy danh sách lệnh sản xuất theo dây chuyền")
    public ResponseEntity<?> getOrdersByLine(@PathVariable Long lineId) {
        List<ProductionOrderDto> orders = orderService.getOrdersByProductionLine(lineId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    @ApiOperation(value = "Lấy danh sách lệnh sản xuất theo trạng thái")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable ProductionOrderStatus status) {
        List<ProductionOrderDto> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    // =================== POST ===================

    @PostMapping
    @ApiOperation(value = "Tạo mới lệnh sản xuất")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateProductionOrderRequest request) {
        ProductionOrderDto dto = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/approve")
    @ApiOperation(value = "Phê duyệt lệnh sản xuất")
    public ResponseEntity<?> approveOrder(@Valid @RequestBody ApproveProductionOrderRequest request) {
        ProductionOrderDto dto = orderService.approveOrder(request);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/start")
    @ApiOperation(value = "Bắt đầu sản xuất")
    public ResponseEntity<?> startProduction(@PathVariable Long id) {
        ProductionOrderDto dto = orderService.startProduction(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/complete")
    @ApiOperation(value = "Hoàn thành sản xuất (chuyển sang chờ QC)")
    public ResponseEntity<?> completeProduction(@PathVariable Long id,
                                                @RequestParam BigDecimal actualQuantity) {
        ProductionOrderDto dto = orderService.completeProduction(id, actualQuantity);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/finish")
    @ApiOperation(value = "Hoàn tất lệnh sản xuất")
    public ResponseEntity<?> finishOrder(@PathVariable Long id) {
        ProductionOrderDto dto = orderService.finishOrder(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/close")
    @ApiOperation(value = "Đóng lệnh sản xuất")
    public ResponseEntity<?> closeOrder(@PathVariable Long id) {
        ProductionOrderDto dto = orderService.closeOrder(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/cancel")
    @ApiOperation(value = "Hủy lệnh sản xuất")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id,
                                         @RequestParam(required = false) String reason) {
        ProductionOrderDto dto = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(dto);
    }

    // =================== PUT ===================

    @PutMapping
    @ApiOperation(value = "Cập nhật thông tin lệnh sản xuất")
    public ResponseEntity<?> updateOrder(@Valid @RequestBody UpdateProductionOrderRequest request) {
        ProductionOrderDto dto = orderService.updateOrder(request);
        return ResponseEntity.ok(dto);
    }
}
