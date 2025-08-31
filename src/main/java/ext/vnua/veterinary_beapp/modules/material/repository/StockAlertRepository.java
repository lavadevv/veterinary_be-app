package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, Long>, JpaSpecificationExecutor<StockAlert> {

    // Tìm theo material ID
    List<StockAlert> findByMaterialId(Long materialId);

    // Tìm theo material batch ID
    List<StockAlert> findByMaterialBatchId(Long materialBatchId);

    // Tìm theo loại cảnh báo
    List<StockAlert> findByAlertType(StockAlert.AlertType alertType);

    // Tìm các cảnh báo chưa được giải quyết
    List<StockAlert> findByIsResolvedFalse();

    // Tìm các cảnh báo đã được giải quyết
    List<StockAlert> findByIsResolvedTrue();

    // Tìm theo material và loại cảnh báo
    List<StockAlert> findByMaterialIdAndAlertType(Long materialId, StockAlert.AlertType alertType);

    // Tìm theo material batch và loại cảnh báo
    List<StockAlert> findByMaterialBatchIdAndAlertType(Long materialBatchId, StockAlert.AlertType alertType);

    // Tìm cảnh báo chưa giải quyết theo material
    List<StockAlert> findByMaterialIdAndIsResolvedFalse(Long materialId);

    // Tìm cảnh báo theo khoảng thời gian
    List<StockAlert> findByAlertDateBetween(LocalDateTime fromDate, LocalDateTime toDate);

    // Tìm cảnh báo theo người giải quyết
    List<StockAlert> findByResolvedById(Long resolvedById);

    // Đếm số cảnh báo chưa giải quyết
    @Query("SELECT COUNT(sa) FROM StockAlert sa WHERE sa.isResolved = false")
    Long countUnresolvedAlerts();

    // Đếm số cảnh báo chưa giải quyết theo loại
    @Query("SELECT COUNT(sa) FROM StockAlert sa WHERE sa.isResolved = false AND sa.alertType = :alertType")
    Long countUnresolvedAlertsByType(@Param("alertType") StockAlert.AlertType alertType);

    // Tìm cảnh báo trùng lặp (cùng material/batch và loại cảnh báo chưa được giải quyết)
    @Query("SELECT sa FROM StockAlert sa WHERE sa.material.id = :materialId " +
            "AND sa.alertType = :alertType AND sa.isResolved = false")
    List<StockAlert> findDuplicateAlerts(@Param("materialId") Long materialId,
                                         @Param("alertType") StockAlert.AlertType alertType);

    @Query("SELECT sa FROM StockAlert sa WHERE sa.materialBatch.id = :materialBatchId " +
            "AND sa.alertType = :alertType AND sa.isResolved = false")
    List<StockAlert> findDuplicateBatchAlerts(@Param("materialBatchId") Long materialBatchId,
                                              @Param("alertType") StockAlert.AlertType alertType);
}
