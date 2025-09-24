package ext.vnua.veterinary_beapp.modules.production.repository;

import ext.vnua.veterinary_beapp.modules.production.enums.ProductionOrderStatus;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long>,
        JpaSpecificationExecutor<ProductionOrder> {

    /**
     * Tìm lệnh sản xuất theo mã lệnh
     */
    Optional<ProductionOrder> findByOrderCode(String orderCode);

    /**
     * Kiểm tra mã lệnh đã tồn tại chưa
     */
    boolean existsByOrderCode(String orderCode);

    /**
     * Tìm các lệnh sản xuất có mã bắt đầu bằng prefix và sắp xếp theo mã giảm dần
     * (dùng để generate mã lệnh mới)
     */
    @Query("SELECT po FROM ProductionOrder po WHERE po.orderCode LIKE :prefix% ORDER BY po.orderCode DESC")
    List<ProductionOrder> findByOrderCodeStartingWithOrderByOrderCodeDesc(@Param("prefix") String prefix);

    /**
     * Tìm lệnh sản xuất theo sản phẩm
     */
    @Query("SELECT po FROM ProductionOrder po WHERE po.product.id = :productId ORDER BY po.createdDate DESC")
    List<ProductionOrder> findByProductIdOrderByCreatedDateDesc(@Param("productId") Long productId);

    /**
     * Tìm lệnh sản xuất theo dây chuyền
     */
    @Query("SELECT po FROM ProductionOrder po WHERE po.productionLine.id = :lineId ORDER BY po.createdDate DESC")
    List<ProductionOrder> findByProductionLineIdOrderByCreatedDateDesc(@Param("lineId") Long lineId);

    /**
     * Tìm lệnh sản xuất theo trạng thái
     */
    List<ProductionOrder> findByStatusOrderByCreatedDateDesc(ProductionOrderStatus status);

    /**
     * Tìm lệnh sản xuất theo khoảng thời gian kế hoạch
     */
    @Query("SELECT po FROM ProductionOrder po WHERE " +
            "(:startDate IS NULL OR po.plannedStartDate >= :startDate) AND " +
            "(:endDate IS NULL OR po.plannedEndDate <= :endDate) " +
            "ORDER BY po.plannedStartDate")
    List<ProductionOrder> findByPlannedDateRange(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * Tìm lệnh sản xuất đang hoạt động (chưa hoàn thành)
     */
    @Query("SELECT po FROM ProductionOrder po WHERE po.status IN ('PLANNED', 'ISSUED', 'IN_PROGRESS', 'QC_PENDING') " +
            "ORDER BY po.createdDate DESC")
    List<ProductionOrder> findActiveOrders();

    /**
     * Tìm lệnh sản xuất cần bắt đầu hôm nay
     */
    @Query("SELECT po FROM ProductionOrder po WHERE po.status = 'ISSUED' AND po.plannedStartDate = :today")
    List<ProductionOrder> findOrdersToStartToday(@Param("today") LocalDate today);

    /**
     * Tìm lệnh sản xuất quá hạn (chưa hoàn thành nhưng đã qua ngày kết thúc)
     */
    @Query("SELECT po FROM ProductionOrder po WHERE " +
            "po.status IN ('ISSUED', 'IN_PROGRESS') AND " +
            "po.plannedEndDate < :currentDate")
    List<ProductionOrder> findOverdueOrders(@Param("currentDate") LocalDate currentDate);

    /**
     * Thống kê số lượng lệnh theo trạng thái
     */
    @Query("SELECT po.status, COUNT(po) FROM ProductionOrder po GROUP BY po.status")
    List<Object[]> countOrdersByStatus();

    /**
     * Thống kê sản lượng theo sản phẩm trong khoảng thời gian
     */
    @Query("SELECT po.product.productCode, po.product.productName, " +
            "SUM(po.plannedQuantity), SUM(po.actualQuantity) " +
            "FROM ProductionOrder po WHERE " +
            "po.actualEndDate BETWEEN :startDate AND :endDate " +
            "GROUP BY po.product.id, po.product.productCode, po.product.productName")
    List<Object[]> getProductionSummaryByProduct(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
}