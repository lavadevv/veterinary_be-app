package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.StockAlertDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.stockAlert.CreateStockAlertRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.stockAlert.UpdateStockAlertRequest;
import ext.vnua.veterinary_beapp.modules.material.model.StockAlert;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomStockAlertQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface StockAlertService {
    Page<StockAlert> getAllStockAlert(CustomStockAlertQuery.StockAlertFilterParam param, PageRequest pageRequest);
    StockAlertDto selectStockAlertById(Long id);
    List<StockAlertDto> selectStockAlertsByMaterial(Long materialId);
    List<StockAlertDto> selectUnresolvedAlerts();

    StockAlertDto createStockAlert(CreateStockAlertRequest request);
    StockAlertDto updateStockAlert(UpdateStockAlertRequest request);

    void deleteStockAlert(Long id);
    List<StockAlertDto> deleteAllIdStockAlerts(List<Long> ids);

    // Business methods
    StockAlertDto resolveAlert(Long alertId, Long userId, String resolutionNotes);
    void createLowStockAlert(Long materialId, Double currentStock, Double minThreshold);
    void createExpiryAlert(Long materialBatchId, LocalDateTime expiryDate);
    void createNearExpiryAlert(Long materialBatchId, LocalDateTime expiryDate, int daysBeforeExpiry);
    void createNegativeStockAlert(Long materialId, Double currentStock);
    void createQuarantineAlert(Long materialBatchId, String reason);

    Long countUnresolvedAlerts();
    Long countUnresolvedAlertsByType(StockAlert.AlertType alertType);
    List<StockAlertDto> getAlertsByDateRange(LocalDateTime fromDate, LocalDateTime toDate);
}
