package ext.vnua.veterinary_beapp.modules.production.services;

import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.*;
import ext.vnua.veterinary_beapp.modules.production.enums.ProductionOrderStatus;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionOrderQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductionOrderService {

    /**
     * Tạo lệnh sản xuất mới
     */
    ProductionOrderDto createOrder(CreateProductionOrderRequest req);

    /**
     * Cập nhật lệnh sản xuất (chỉ trạng thái PLANNED)
     */
    ProductionOrderDto updateOrder(UpdateProductionOrderRequest req);

    /**
     * Phê duyệt lệnh sản xuất
     */
    ProductionOrderDto approveOrder(ApproveProductionOrderRequest req);

    /**
     * Bắt đầu sản xuất
     */
    ProductionOrderDto startProduction(Long orderId);

    /**
     * Hoàn thành sản xuất với sản lượng thực tế
     */
    ProductionOrderDto completeProduction(Long orderId, BigDecimal actualQuantity);

    /**
     * Hoàn tất lệnh sản xuất (sau QC)
     */
    ProductionOrderDto finishOrder(Long orderId);

    /**
     * Đóng lệnh sản xuất
     */
    ProductionOrderDto closeOrder(Long orderId);

    /**
     * Hủy lệnh sản xuất
     */
    ProductionOrderDto cancelOrder(Long orderId, String reason);

    /**
     * Lấy lệnh sản xuất theo ID
     */
    ProductionOrderDto getById(Long id);

    /**
     * Lấy lệnh sản xuất theo mã lệnh
     */
    ProductionOrderDto getByOrderCode(String orderCode);

    /**
     * Tìm kiếm lệnh sản xuất với bộ lọc
     */
    Page<ProductionOrder> searchOrders(GetProductionOrderRequest filter, Pageable pageable);

    /**
     * Lấy danh sách lệnh sản xuất theo sản phẩm
     */
    List<ProductionOrderDto> getOrdersByProduct(Long productId);

    /**
     * Lấy danh sách lệnh sản xuất theo dây chuyền
     */
    List<ProductionOrderDto> getOrdersByProductionLine(Long lineId);

    /**
     * Lấy danh sách lệnh sản xuất theo trạng thái
     */
    List<ProductionOrderDto> getOrdersByStatus(ProductionOrderStatus status);
}