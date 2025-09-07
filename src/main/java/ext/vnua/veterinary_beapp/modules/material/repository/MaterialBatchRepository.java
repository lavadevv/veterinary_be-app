package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
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

    @EntityGraph(attributePaths = {"material", "material.supplier", "location"})
    List<MaterialBatch> findAll();

    @EntityGraph(attributePaths = {"material", "material.supplier", "location", "location.warehouse"})
    Optional<MaterialBatch> findById(Long id);

    @Query("SELECT mb FROM MaterialBatch mb JOIN FETCH mb.material")
    List<MaterialBatch> findAllWithMaterial();


    // Tìm theo số lô
    Optional<MaterialBatch> findByBatchNumber(String batchNumber);

    // Tìm theo mã lô nội bộ
    Optional<MaterialBatch> findByInternalBatchCode(String internalBatchCode);

    // Tìm theo vật liệu
    @EntityGraph(attributePaths = {"material", "material.supplier", "location"})
    List<MaterialBatch> findByMaterialId(Long materialId);

    // Tìm theo vị trí
    List<MaterialBatch> findByLocationId(Long locationId);

    // Tìm theo trạng thái kiểm nghiệm
    List<MaterialBatch> findByTestStatus(TestStatus testStatus);

    // Tìm theo trạng thái sử dụng
    List<MaterialBatch> findByUsageStatus(UsageStatus usageStatus);

    // Tìm lô hết hạn
    @Query("SELECT mb FROM MaterialBatch mb WHERE mb.expiryDate < :currentDate")
    List<MaterialBatch> findExpiredBatches(@Param("currentDate") LocalDate currentDate);

    // Tìm lô sắp hết hạn (trong vòng 30 ngày)
    @Query("SELECT mb FROM MaterialBatch mb WHERE mb.expiryDate BETWEEN :currentDate AND :futureDate")
    List<MaterialBatch> findBatchesNearExpiry(@Param("currentDate") LocalDate currentDate,
                                              @Param("futureDate") LocalDate futureDate);

    // Tìm lô có thể sử dụng (đã kiểm nghiệm xong và còn hạn)
    @Query("SELECT mb FROM MaterialBatch mb WHERE " +
            "mb.testStatus = :approvedStatus AND " +
            "mb.usageStatus = :availableStatus AND " +
            "mb.expiryDate > :currentDate AND " +
            "mb.currentQuantity > 0")
    List<MaterialBatch> findUsableBatches(@Param("approvedStatus") TestStatus approvedStatus,
                                          @Param("availableStatus") UsageStatus availableStatus,
                                          @Param("currentDate") LocalDate currentDate);

    // Kiểm tra mã lô nội bộ trùng (loại trừ ID hiện tại)
    Optional<MaterialBatch> findByInternalBatchCodeAndIdNot(String internalBatchCode, Long id);

    // Tổng số lượng theo vật liệu
    @Query("SELECT SUM(mb.currentQuantity) FROM MaterialBatch mb WHERE mb.material.id = :materialId")
    BigDecimal getTotalQuantityByMaterial(@Param("materialId") Long materialId);

    // Tìm lô theo vật liệu và vị trí
    List<MaterialBatch> findByMaterialIdAndLocationId(Long materialId, Long locationId);

    // Tìm lô có số lượng lớn hơn 0
    @Query("SELECT mb FROM MaterialBatch mb WHERE mb.currentQuantity > 0")
    List<MaterialBatch> findBatchesWithStock();

    // Lấy lô cũ nhất theo FIFO (First In, First Out)
    @Query("SELECT mb FROM MaterialBatch mb WHERE " +
            "mb.material.id = :materialId AND " +
            "mb.currentQuantity > 0 AND " +
            "mb.testStatus = :approvedStatus AND " +
            "mb.usageStatus = :availableStatus " +
            "ORDER BY mb.receivedDate ASC")
    List<MaterialBatch> findOldestUsableBatches(@Param("materialId") Long materialId,
                                                @Param("approvedStatus") TestStatus approvedStatus,
                                                @Param("availableStatus") UsageStatus availableStatus);
}
