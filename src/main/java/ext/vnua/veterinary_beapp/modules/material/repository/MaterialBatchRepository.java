package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
public interface MaterialBatchRepository extends JpaRepository<MaterialBatch, Long>,
        JpaSpecificationExecutor<MaterialBatch> {

    String GRAPH = MaterialBatch.ENTITY_GRAPH_WITH_DETAILS;

    @Override
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    Page<MaterialBatch> findAll(org.springframework.data.jpa.domain.Specification<MaterialBatch> spec, Pageable pageable);

    @Override
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    List<MaterialBatch> findAll();

    @Override
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    Optional<MaterialBatch> findById(Long id);

    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT mb FROM MaterialBatch mb")
    List<MaterialBatch> findAllWithMaterial();

    // Tìm theo số lô
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    Optional<MaterialBatch> findByBatchNumber(String batchNumber);

    // Check if batch number exists
    boolean existsByBatchNumber(String batchNumber);

    // Tìm theo mã lô nội bộ
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    Optional<MaterialBatch> findByInternalBatchCode(String internalBatchCode);

    // Check if internal batch code exists
    boolean existsByInternalBatchCode(String internalBatchCode);

    // Tìm theo vị trí
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    List<MaterialBatch> findByLocationId(Long locationId);

    // Kiểm tra mã lô nội bộ trùng (loại trừ ID hiện tại)
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    Optional<MaterialBatch> findByInternalBatchCodeAndIdNot(String internalBatchCode, Long id);

    // thêm vào MaterialBatchRepository
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    @Query("""
       select mb from MaterialBatch mb
       where mb.receivedDate between :start and :end
    """)
    List<MaterialBatch> findByReceivedDateBetween(java.time.LocalDate start, java.time.LocalDate end);

    // TODO: Deprecated queries below reference fields that moved to MaterialBatchItem
    // These need to be refactored or replaced with MaterialBatchItemRepository queries

    /* DEPRECATED - MaterialBatch no longer has material field
    // Tìm theo vật liệu
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    List<MaterialBatch> findByMaterialId(Long materialId);
    */

    /* DEPRECATED - testStatus is now on MaterialBatchItem
    // Tìm theo trạng thái kiểm nghiệm
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    List<MaterialBatch> findByTestStatus(TestStatus testStatus);

    // Tìm theo trạng thái sử dụng
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    List<MaterialBatch> findByUsageStatus(UsageStatus usageStatus);
    */

    /* DEPRECATED - MaterialBatch no longer has material field
    // Tìm lô theo vật liệu và vị trí
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    List<MaterialBatch> findByMaterialIdAndLocationId(Long materialId, Long locationId);
    */

    /* DEPRECATED - expiryDate is now on MaterialBatchItem
    // Tìm lô hết hạn
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT mb FROM MaterialBatch mb WHERE mb.expiryDate < :currentDate")
    List<MaterialBatch> findExpiredBatches(@Param("currentDate") LocalDate currentDate);

    // Tìm lô sắp hết hạn (trong vòng 30 ngày)
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT mb FROM MaterialBatch mb WHERE mb.expiryDate BETWEEN :currentDate AND :futureDate")
    List<MaterialBatch> findBatchesNearExpiry(@Param("currentDate") LocalDate currentDate,
                                              @Param("futureDate") LocalDate futureDate);
    */

    /* DEPRECATED - testStatus, usageStatus, currentQuantity are now on MaterialBatchItem
    // Tìm lô có thể sử dụng (đã kiểm nghiệm xong và còn hạn)
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT mb FROM MaterialBatch mb WHERE " +
            "mb.testStatus = :approvedStatus AND " +
            "mb.usageStatus = :availableStatus AND " +
            "mb.expiryDate > :currentDate AND " +
            "mb.currentQuantity > 0")
    List<MaterialBatch> findUsableBatches(@Param("approvedStatus") TestStatus approvedStatus,
                                          @Param("availableStatus") UsageStatus availableStatus,
                                          @Param("currentDate") LocalDate currentDate);
    */

    /* DEPRECATED - material, currentQuantity are now on MaterialBatchItem
    // Tổng số lượng theo vật liệu
    @Query("SELECT SUM(mb.currentQuantity) FROM MaterialBatch mb WHERE mb.material.id = :materialId")
    BigDecimal getTotalQuantityByMaterial(@Param("materialId") Long materialId);
    */

    /* DEPRECATED - currentQuantity is now on MaterialBatchItem
    // Tìm lô có số lượng lớn hơn 0
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT mb FROM MaterialBatch mb WHERE mb.currentQuantity > 0")
    List<MaterialBatch> findBatchesWithStock();
    */

    /* DEPRECATED - material, currentQuantity, testStatus, usageStatus are now on MaterialBatchItem
    // Lấy lô cũ nhất theo FIFO (First In, First Out)
    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT mb FROM MaterialBatch mb WHERE " +
            "mb.material.id = :materialId AND " +
            "mb.currentQuantity > 0 AND " +
            "mb.testStatus = :approvedStatus AND " +
            "mb.usageStatus = :availableStatus " +
            "ORDER BY mb.receivedDate ASC")
    List<MaterialBatch> findOldestUsableBatches(@Param("materialId") Long materialId,
                                                @Param("approvedStatus") TestStatus approvedStatus,
                                                @Param("availableStatus") UsageStatus availableStatus);

    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT mb FROM MaterialBatch mb " +
            "WHERE mb.material.id = :materialId AND mb.availableQuantity > 0 " +
            "ORDER BY mb.expiryDate ASC, mb.receivedDate ASC")
    List<MaterialBatch> findAvailableByMaterialFifo(@Param("materialId") Long materialId);

    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    List<MaterialBatch> findByMaterialIdAndTestStatusAndUsageStatusAndExpiryDateGreaterThanEqualOrderByExpiryDateAscManufacturingDateAscIdAsc(
            Long materialId, TestStatus testStatus, UsageStatus usageStatus, LocalDate today);

    @EntityGraph(value = GRAPH, type = EntityGraph.EntityGraphType.LOAD)
    @Query("""
        SELECT mb FROM MaterialBatch mb
        WHERE mb.material.id = :materialId
          AND mb.availableQuantity > 0
          AND mb.testStatus = :approvedStatus
          AND mb.usageStatus = :availableStatus
          AND (mb.expiryDate IS NULL OR mb.expiryDate >= :today)
        ORDER BY
          CASE WHEN mb.expiryDate IS NULL THEN 1 ELSE 0 END ASC,
          mb.expiryDate ASC,
          mb.receivedDate ASC,
          mb.id ASC
    """)
    List<MaterialBatch> findAvailableByMaterialFifoUsable(
            @Param("materialId") Long materialId,
            @Param("approvedStatus") ext.vnua.veterinary_beapp.modules.material.enums.TestStatus approvedStatus,
            @Param("availableStatus") ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus availableStatus,
            @Param("today") java.time.LocalDate today
    );
    */

}
