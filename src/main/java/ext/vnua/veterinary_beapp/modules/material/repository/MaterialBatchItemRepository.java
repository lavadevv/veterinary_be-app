package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItem;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialBatchItemRepository extends JpaRepository<MaterialBatchItem, Long>, 
                                                      JpaSpecificationExecutor<MaterialBatchItem> {

    /**
     * Tìm tất cả items của một vật liệu cụ thể
     */
    List<MaterialBatchItem> findByMaterial(Material material);

    /**
     * Tìm tất cả items của một vật liệu cụ thể theo materialId
     */
    List<MaterialBatchItem> findByMaterialId(Long materialId);

    /**
     * Tìm items theo vật liệu và location
     */
    List<MaterialBatchItem> findByMaterialAndLocation(Material material, Location location);

    /**
     * Tìm items có số lượng khả dụng lớn hơn giá trị cho trước
     */
    List<MaterialBatchItem> findByMaterialAndAvailableQuantityGreaterThan(
            Material material, BigDecimal minQuantity);

    /**
     * Tìm items theo vật liệu và trạng thái sử dụng
     */
    List<MaterialBatchItem> findByMaterialAndUsageStatus(Material material, UsageStatus usageStatus);

    /**
     * Tìm items theo vật liệu, trạng thái sử dụng và có số lượng khả dụng
     */
    @Query("SELECT mbi FROM MaterialBatchItem mbi " +
           "WHERE mbi.material = :material " +
           "AND mbi.usageStatus = :usageStatus " +
           "AND mbi.availableQuantity > 0 " +
           "ORDER BY mbi.expiryDate ASC")
    List<MaterialBatchItem> findAvailableItemsByMaterialAndStatus(
            @Param("material") Material material, 
            @Param("usageStatus") UsageStatus usageStatus);

    /**
     * Tìm items sắp hết hạn (trong vòng N ngày)
     */
    @Query("SELECT mbi FROM MaterialBatchItem mbi " +
           "WHERE mbi.expiryDate BETWEEN :fromDate AND :toDate " +
           "AND mbi.currentQuantity > 0 " +
           "ORDER BY mbi.expiryDate ASC")
    List<MaterialBatchItem> findItemsExpiringBetween(
            @Param("fromDate") LocalDate fromDate, 
            @Param("toDate") LocalDate toDate);

    /**
     * Tìm items đã hết hạn
     */
    @Query("SELECT mbi FROM MaterialBatchItem mbi " +
           "WHERE mbi.expiryDate < :date " +
           "AND mbi.currentQuantity > 0")
    List<MaterialBatchItem> findExpiredItems(@Param("date") LocalDate date);

    /**
     * Tìm items theo internal code
     */
    Optional<MaterialBatchItem> findByInternalItemCode(String internalItemCode);

    /**
     * Tìm items theo manufacturer batch number
     */
    List<MaterialBatchItem> findByManufacturerBatchNumber(String manufacturerBatchNumber);

    /**
     * Tìm items theo batch id
     */
    List<MaterialBatchItem> findByBatchId(Long batchId);

    /**
     * Tính tổng số lượng khả dụng của một vật liệu
     */
    @Query("SELECT COALESCE(SUM(mbi.availableQuantity), 0) FROM MaterialBatchItem mbi " +
           "WHERE mbi.material.id = :materialId " +
           "AND mbi.usageStatus = 'SAN_SANG'")
    BigDecimal calculateTotalAvailableQuantity(@Param("materialId") Long materialId);

    /**
     * Tính tổng số lượng hiện tại của một vật liệu
     */
    @Query("SELECT COALESCE(SUM(mbi.currentQuantity), 0) FROM MaterialBatchItem mbi " +
           "WHERE mbi.material.id = :materialId")
    BigDecimal calculateTotalCurrentQuantity(@Param("materialId") Long materialId);

    /**
     * Tìm items theo test status
     */
    List<MaterialBatchItem> findByTestStatus(TestStatus testStatus);

    /**
     * Tìm items theo location
     */
    List<MaterialBatchItem> findByLocation(Location location);

    /**
     * Tìm items theo location và có số lượng > 0
     */
    @Query("SELECT mbi FROM MaterialBatchItem mbi " +
           "WHERE mbi.location = :location " +
           "AND mbi.currentQuantity > 0")
    List<MaterialBatchItem> findByLocationWithStock(@Param("location") Location location);

    /**
     * Tìm items FIFO (First In First Out) cho allocation
     * Ưu tiên: hạn sử dụng sớm nhất, ngày nhập sớm nhất
     */
    @Query("SELECT mbi FROM MaterialBatchItem mbi " +
           "WHERE mbi.material.id = :materialId " +
           "AND mbi.usageStatus = 'SAN_SANG' " +
           "AND mbi.availableQuantity > 0 " +
           "ORDER BY mbi.expiryDate ASC, mbi.batch.receivedDate ASC")
    List<MaterialBatchItem> findFIFOItemsForAllocation(@Param("materialId") Long materialId);

    /**
     * Tìm items FEFO (First Expired First Out)
     */
    @Query("SELECT mbi FROM MaterialBatchItem mbi " +
           "WHERE mbi.material.id = :materialId " +
           "AND mbi.usageStatus = 'SAN_SANG' " +
           "AND mbi.availableQuantity > 0 " +
           "AND mbi.expiryDate > :currentDate " +
           "ORDER BY mbi.expiryDate ASC")
    List<MaterialBatchItem> findFEFOItemsForAllocation(
            @Param("materialId") Long materialId,
            @Param("currentDate") LocalDate currentDate);

    /**
     * Đếm số lượng items trong một batch
     */
    @Query("SELECT COUNT(mbi) FROM MaterialBatchItem mbi WHERE mbi.batch.id = :batchId")
    long countItemsByBatchId(@Param("batchId") Long batchId);

    /**
     * Kiểm tra xem internal item code đã tồn tại chưa
     */
    boolean existsByInternalItemCode(String internalItemCode);
}
