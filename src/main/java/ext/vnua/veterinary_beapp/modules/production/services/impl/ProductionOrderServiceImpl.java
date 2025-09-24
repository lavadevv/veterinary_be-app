package ext.vnua.veterinary_beapp.modules.production.services.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.*;
import ext.vnua.veterinary_beapp.modules.production.enums.ProductionOrderStatus;
import ext.vnua.veterinary_beapp.modules.production.mapper.ProductionOrderMapper;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionLine;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import ext.vnua.veterinary_beapp.modules.production.repository.ProductionLineRepository;
import ext.vnua.veterinary_beapp.modules.production.repository.ProductionOrderRepository;
import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionOrderQuery;
import ext.vnua.veterinary_beapp.modules.production.services.ProductionOrderService;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductRepository;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionOrderServiceImpl implements ProductionOrderService {

    private final ProductionOrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final ProductionLineRepository lineRepo;
    private final UserRepository userRepo;
    private final ProductionOrderMapper orderMapper;

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductionOrder", description = "Tạo mới lệnh sản xuất")
    public ProductionOrderDto createOrder(CreateProductionOrderRequest req) {
        log.info("Creating production order for product ID: {}", req.getProductId());

        // Validate product exists and is active
        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new DataExistException("Sản phẩm không tồn tại"));

        if (!product.getIsActive()) {
            throw new IllegalStateException("Không thể tạo lệnh sản xuất cho sản phẩm đã ngừng hoạt động");
        }

        // Validate production line if provided
        ProductionLine line = null;
        if (req.getProductionLineId() != null) {
            line = lineRepo.findById(req.getProductionLineId())
                    .orElseThrow(() -> new DataExistException("Dây chuyền không tồn tại"));

            if (!"ACTIVE".equals(line.getStatus())) {
                throw new IllegalStateException("Dây chuyền không hoạt động");
            }
        }

        // Validate date constraints
        validateDateConstraints(req.getPlannedStartDate(), req.getPlannedEndDate());

        ProductionOrder order = new ProductionOrder();
        order.setOrderCode(generateOrderCode(product.getProductCode()));
        order.setProduct(product);
        order.setPlannedQuantity(req.getPlannedQuantity());
        order.setPlannedStartDate(req.getPlannedStartDate());
        order.setPlannedEndDate(req.getPlannedEndDate());
        order.setProductionLine(line);
        order.setStatus(ProductionOrderStatus.PLANNED);
        order.setNotes(req.getNotes());

        ProductionOrder savedOrder = orderRepo.save(order);
        log.info("Created production order with code: {}", savedOrder.getOrderCode());

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrder", description = "Cập nhật lệnh sản xuất")
    public ProductionOrderDto updateOrder(UpdateProductionOrderRequest req) {
        log.info("Updating production order ID: {}", req.getId());

        ProductionOrder order = orderRepo.findById(req.getId())
                .orElseThrow(() -> new DataExistException("Lệnh sản xuất không tồn tại"));

        // Validate status - only PLANNED orders can be updated
        if (order.getStatus() != ProductionOrderStatus.PLANNED) {
            throw new IllegalStateException("Chỉ có thể sửa lệnh sản xuất ở trạng thái 'Kế hoạch'");
        }

        // Update fields if provided
        if (req.getPlannedQuantity() != null) {
            if (req.getPlannedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Sản lượng kế hoạch phải > 0");
            }
            order.setPlannedQuantity(req.getPlannedQuantity());
        }

        if (req.getPlannedStartDate() != null) {
            order.setPlannedStartDate(req.getPlannedStartDate());
        }

        if (req.getPlannedEndDate() != null) {
            order.setPlannedEndDate(req.getPlannedEndDate());
        }

        // Validate date constraints after update
        validateDateConstraints(order.getPlannedStartDate(), order.getPlannedEndDate());

        if (req.getProductionLineId() != null) {
            ProductionLine line = lineRepo.findById(req.getProductionLineId())
                    .orElseThrow(() -> new DataExistException("Dây chuyền không tồn tại"));

            if (!"ACTIVE".equals(line.getStatus())) {
                throw new IllegalStateException("Dây chuyền không hoạt động");
            }
            order.setProductionLine(line);
        }

        if (req.getNotes() != null) {
            order.setNotes(req.getNotes());
        }

        ProductionOrder savedOrder = orderRepo.save(order);
        log.info("Updated production order: {}", savedOrder.getOrderCode());

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrder", description = "Phê duyệt lệnh sản xuất")
    public ProductionOrderDto approveOrder(ApproveProductionOrderRequest req) {
        log.info("Approving production order ID: {}", req.getOrderId());

        ProductionOrder order = orderRepo.findById(req.getOrderId())
                .orElseThrow(() -> new DataExistException("Lệnh sản xuất không tồn tại"));

        // Validate current status
        if (order.getStatus() != ProductionOrderStatus.PLANNED) {
            throw new IllegalStateException("Chỉ có thể phê duyệt lệnh ở trạng thái 'Kế hoạch'");
        }

        // Validate approver
        User approver = userRepo.findById(req.getApprovedById())
                .orElseThrow(() -> new DataExistException("Người phê duyệt không tồn tại"));

        // Business validation - ensure all required fields are present
        if (order.getPlannedQuantity() == null || order.getPlannedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Lệnh sản xuất phải có sản lượng kế hoạch > 0");
        }

        if (order.getPlannedStartDate() == null || order.getPlannedEndDate() == null) {
            throw new IllegalStateException("Lệnh sản xuất phải có ngày bắt đầu và kết thúc");
        }

        order.setStatus(ProductionOrderStatus.ISSUED);
        order.setApprovedByUser(approver);

        ProductionOrder savedOrder = orderRepo.save(order);
        log.info("Approved production order: {} by user: {}",
                savedOrder.getOrderCode(), approver.getEmail());

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrder", description = "Bắt đầu sản xuất")
    public ProductionOrderDto startProduction(Long orderId) {
        log.info("Starting production for order ID: {}", orderId);

        ProductionOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new DataExistException("Lệnh sản xuất không tồn tại"));

        // Validate current status
        if (order.getStatus() != ProductionOrderStatus.ISSUED) {
            throw new IllegalStateException("Chỉ có thể bắt đầu sản xuất với lệnh đã được phê duyệt");
        }

        order.setStatus(ProductionOrderStatus.IN_PROGRESS);
        order.setActualStartDate(LocalDate.now());

        ProductionOrder savedOrder = orderRepo.save(order);
        log.info("Started production for order: {}", savedOrder.getOrderCode());

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrder", description = "Hoàn thành sản xuất (chờ QC)")
    public ProductionOrderDto completeProduction(Long orderId, BigDecimal actualQuantity) {
        log.info("Completing production for order ID: {} with quantity: {}", orderId, actualQuantity);

        ProductionOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new DataExistException("Lệnh sản xuất không tồn tại"));

        // Validate current status
        if (order.getStatus() != ProductionOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Chỉ có thể hoàn thành lệnh đang trong quá trình sản xuất");
        }

        if (actualQuantity == null || actualQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Sản lượng thực tế phải > 0");
        }

        order.setStatus(ProductionOrderStatus.QC_PENDING);
        order.setActualQuantity(actualQuantity);
        order.setActualEndDate(LocalDate.now());

        // Calculate yield rate
        BigDecimal yieldRate = actualQuantity.divide(order.getPlannedQuantity(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        order.setYieldRate(yieldRate);

        ProductionOrder savedOrder = orderRepo.save(order);
        log.info("Completed production for order: {} with yield rate: {}%",
                savedOrder.getOrderCode(), yieldRate);

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrder", description = "Hoàn tất lệnh sản xuất")
    public ProductionOrderDto finishOrder(Long orderId) {
        log.info("Finishing order ID: {}", orderId);

        ProductionOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new DataExistException("Lệnh sản xuất không tồn tại"));

        // Can finish from QC_PENDING or IN_PROGRESS status
        if (order.getStatus() != ProductionOrderStatus.QC_PENDING &&
                order.getStatus() != ProductionOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Chỉ có thể hoàn tất lệnh từ trạng thái 'Chờ QC' hoặc 'Đang sản xuất'");
        }

        order.setStatus(ProductionOrderStatus.COMPLETED);
        if (order.getActualEndDate() == null) {
            order.setActualEndDate(LocalDate.now());
        }

        ProductionOrder savedOrder = orderRepo.save(order);
        log.info("Finished order: {}", savedOrder.getOrderCode());

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrder", description = "Đóng lệnh sản xuất")
    public ProductionOrderDto closeOrder(Long orderId) {
        log.info("Closing order ID: {}", orderId);

        ProductionOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new DataExistException("Lệnh sản xuất không tồn tại"));

        // Can only close completed orders
        if (order.getStatus() != ProductionOrderStatus.COMPLETED) {
            throw new IllegalStateException("Chỉ có thể đóng lệnh đã hoàn thành");
        }

        order.setStatus(ProductionOrderStatus.CLOSED);

        ProductionOrder savedOrder = orderRepo.save(order);
        log.info("Closed order: {}", savedOrder.getOrderCode());

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrder", description = "Hủy lệnh sản xuất")
    public ProductionOrderDto cancelOrder(Long orderId, String reason) {
        log.info("Cancelling order ID: {} with reason: {}", orderId, reason);

        ProductionOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new DataExistException("Lệnh sản xuất không tồn tại"));

        // Can only cancel orders that are not completed or closed
        if (order.getStatus() == ProductionOrderStatus.COMPLETED ||
                order.getStatus() == ProductionOrderStatus.CLOSED) {
            throw new IllegalStateException("Không thể hủy lệnh đã hoàn thành hoặc đã đóng");
        }

        order.setStatus(ProductionOrderStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            String cancelNote = "HỦY: " + reason.trim();
            order.setNotes(order.getNotes() != null ?
                    order.getNotes() + "\n" + cancelNote : cancelNote);
        }

        ProductionOrder savedOrder = orderRepo.save(order);
        log.info("Cancelled order: {}", savedOrder.getOrderCode());

        return orderMapper.toDto(savedOrder);
    }

    @Override
    public ProductionOrderDto getById(Long id) {
        ProductionOrder order = orderRepo.findById(id)
                .orElseThrow(() -> new DataExistException("Lệnh sản xuất không tồn tại"));
        return orderMapper.toDto(order);
    }

    @Override
    public ProductionOrderDto getByOrderCode(String orderCode) {
        ProductionOrder order = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new DataExistException("Lệnh sản xuất không tồn tại với mã: " + orderCode));
        return orderMapper.toDto(order);
    }

    @Override
    public Page<ProductionOrder> searchOrders(GetProductionOrderRequest filter,
                                              Pageable pageable) {
        Specification<ProductionOrder> spec = CustomProductionOrderQuery.getFilter(filter);
        return orderRepo.findAll(spec, pageable);
    }

    @Override
    public List<ProductionOrderDto> getOrdersByProduct(Long productId) {
        List<ProductionOrder> orders = orderRepo.findByProductIdOrderByCreatedDateDesc(productId);
        return orders.stream().map(orderMapper::toDto).toList();
    }

    @Override
    public List<ProductionOrderDto> getOrdersByProductionLine(Long lineId) {
        List<ProductionOrder> orders = orderRepo.findByProductionLineIdOrderByCreatedDateDesc(lineId);
        return orders.stream().map(orderMapper::toDto).toList();
    }

    @Override
    public List<ProductionOrderDto> getOrdersByStatus(ProductionOrderStatus status) {
        List<ProductionOrder> orders = orderRepo.findByStatusOrderByCreatedDateDesc(status);
        return orders.stream().map(orderMapper::toDto).toList();
    }

    // Private helper methods

    private String generateOrderCode(String productCode) {
        LocalDate now = LocalDate.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Find the highest sequence number for today
        String prefix = "PO-" + dateStr + "-";
        List<ProductionOrder> ordersToday = orderRepo.findByOrderCodeStartingWithOrderByOrderCodeDesc(prefix);

        int sequence = 1;
        if (!ordersToday.isEmpty()) {
            String lastCode = ordersToday.get(0).getOrderCode();
            String sequenceStr = lastCode.substring(lastCode.lastIndexOf("-") + 1);
            try {
                sequence = Integer.parseInt(sequenceStr) + 1;
            } catch (NumberFormatException e) {
                log.warn("Cannot parse sequence from order code: {}", lastCode);
                sequence = 1;
            }
        }

        return prefix + String.format("%03d", sequence);
    }

    private void validateDateConstraints(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
            }

            // Business rule: production orders should not be planned too far in the past
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            if (startDate.isBefore(thirtyDaysAgo)) {
                throw new IllegalArgumentException("Không thể lập kế hoạch sản xuất quá xa trong quá khứ");
            }
        }
    }
}