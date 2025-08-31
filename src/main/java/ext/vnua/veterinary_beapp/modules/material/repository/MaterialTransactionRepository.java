package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialTransactionRepository extends JpaRepository<MaterialTransaction, Long>, JpaSpecificationExecutor<MaterialTransaction> {

    // Tìm theo material batch
    List<MaterialTransaction> findByMaterialBatchId(Long materialBatchId);

    // Tìm theo loại giao dịch
    List<MaterialTransaction> findByTransactionType(MaterialTransaction.TransactionType transactionType);

    // Tìm theo khoảng thời gian
    List<MaterialTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Tìm theo material batch và loại giao dịch
    List<MaterialTransaction> findByMaterialBatchIdAndTransactionType(Long materialBatchId, MaterialTransaction.TransactionType transactionType);

    // Tìm theo reference document
    Optional<MaterialTransaction> findByReferenceDocument(String referenceDocument);

    // Tìm theo production order
    List<MaterialTransaction> findByProductionOrderId(String productionOrderId);

    // Tìm theo from location
    List<MaterialTransaction> findByFromLocationId(Long fromLocationId);

    // Tìm theo to location
    List<MaterialTransaction> findByToLocationId(Long toLocationId);

    // Query để tính tổng quantity theo type cho một material batch
    @Query("SELECT SUM(mt.quantity) FROM MaterialTransaction mt " +
            "WHERE mt.materialBatch.id = :materialBatchId " +
            "AND mt.transactionType = :transactionType")
    Double sumQuantityByMaterialBatchAndType(@Param("materialBatchId") Long materialBatchId,
                                             @Param("transactionType") MaterialTransaction.TransactionType transactionType);

    // Query để lấy transactions trong khoảng thời gian cho một warehouse
    @Query("""
        SELECT mt FROM MaterialTransaction mt
        JOIN mt.materialBatch mb
        JOIN mb.location l
        JOIN l.warehouse w
        WHERE w.id = :warehouseId
          AND mt.transactionDate BETWEEN :startDate AND :endDate
        ORDER BY mt.transactionDate DESC
    """)
    List<MaterialTransaction> findWarehouseTransactionsByDateRange(
            @Param("warehouseId") Long warehouseId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
