package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.BatchQuantityAdjustmentRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.BatchTransferRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateMaterialBatchContainerRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateMaterialBatchItemRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateMaterialBatchRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.UpdateMaterialBatchRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.response.MaterialBatchDetailDTO;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialBatchQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MaterialBatchService {
    Page<MaterialBatchDto> getAllMaterialBatch(CustomMaterialBatchQuery.MaterialBatchFilterParam param,
                                               PageRequest pageRequest);

    MaterialBatchDto selectMaterialBatchById(Long id);
    MaterialBatchDto selectMaterialBatchByBatchNumber(String batchNumber);
    MaterialBatchDto selectMaterialBatchByInternalCode(String internalCode);

    List<MaterialBatchDto> selectMaterialBatchesByMaterial(Long materialId);
    List<MaterialBatchDto> selectMaterialBatchesByLocation(Long locationId);

    MaterialBatchDto createMaterialBatch(CreateMaterialBatchRequest request);
    MaterialBatchDto createMaterialBatchContainer(CreateMaterialBatchContainerRequest request);
    MaterialBatchDto addItemToBatch(Long batchId, CreateMaterialBatchItemRequest request);
    MaterialBatchDto updateMaterialBatch(UpdateMaterialBatchRequest request);

    void deleteMaterialBatch(Long id);
    List<MaterialBatchDto> deleteAllIdMaterialBatches(List<Long> ids);

    // Business methods
    void updateQuantity(Long batchId, BigDecimal newQuantity);
    void updateTestStatus(Long batchId, String testStatus);
    void updateUsageStatus(Long batchId, String usageStatus);
    void moveToLocation(Long batchId, Long newLocationId);

    List<MaterialBatchDto> getExpiredBatches();
    List<MaterialBatchDto> getBatchesNearExpiry();
    List<MaterialBatchDto> getUsableBatches();

    BigDecimal getTotalQuantityByMaterial(Long materialId);
    List<MaterialBatchDto> getOldestUsableBatches(Long materialId);

    // Batch operations
    List<MaterialBatchDto> transferBatches(BatchTransferRequest request);
    void adjustBatchQuantity(BatchQuantityAdjustmentRequest request);
    void markBatchAsExpired(Long batchId);
    void markBatchAsConsumed(Long batchId);

    // Reporting methods
    List<MaterialBatchDto> getLowStockBatches(BigDecimal threshold);
    List<MaterialBatchDto> getBatchesByDateRange(LocalDate startDate, LocalDate endDate);
    BigDecimal calculateTotalValue(); // Total inventory value
    BigDecimal calculateTotalValueByMaterial(Long materialId);

    // Quality control
    List<MaterialBatchDto> getPendingTestBatches();
    List<MaterialBatchDto> getFailedTestBatches();
    void approveTestBatch(Long batchId, String testResults);
    void rejectTestBatch(Long batchId, String rejectionReason);

    // ========== NEW: Batch Detail with Active Ingredients ==========
    
    /**
     * Lấy thông tin chi tiết lô bao gồm hoạt chất, COA, KQPT
     */
    MaterialBatchDetailDTO getBatchDetailWithIngredients(Long batchId);
    
    /**
     * Lấy danh sách lô với thông tin đầy đủ
     */
    List<MaterialBatchDetailDTO> getAllBatchesWithDetails();
    
    /**
     * Lấy danh sách lô có hoạt chất không đạt chuẩn (KQPT/COA < 90% hoặc > 110%)
     */
    List<MaterialBatchDetailDTO> getBatchesWithUnqualifiedIngredients();
}

