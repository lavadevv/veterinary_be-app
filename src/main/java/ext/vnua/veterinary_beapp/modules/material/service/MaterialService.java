package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchDto;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.CreateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.UpdateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.BatchQuantityAdjustmentRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.BatchTransferRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MaterialService {
    Page<Material> getAllMaterial(CustomMaterialQuery.MaterialFilterParam param, PageRequest pageRequest);
    MaterialDto selectMaterialById(Long id);
    MaterialDto selectMaterialByCode(String materialCode);
    List<MaterialDto> selectMaterialsBySupplier(Long supplierId);
    List<?> getMaterialActiveIngredients(Long materialId);

    MaterialDto createMaterial(CreateMaterialRequest request);
    MaterialDto updateMaterial(UpdateMaterialRequest request);

    void deleteMaterial(Long id);
    List<MaterialDto> deleteAllIdMaterials(List<Long> ids);

    // Additional business methods
    void updateCurrentStock(Long materialId, Double newStock);
    void toggleActiveStatus(Long materialId);
    List<MaterialDto> getLowStockMaterials();
    List<MaterialDto> getMaterialsRequiringColdStorage();
    List<MaterialDto> getActiveMaterials();
    void syncMaterialStock(Long materialId);
//    List<MaterialBatchDto> transferBatches(BatchTransferRequest request);
//    void adjustBatchQuantity(BatchQuantityAdjustmentRequest request);
//    void markBatchAsExpired(Long batchId);
//    void markBatchAsConsumed(Long batchId);
//
//    //extends
//
//    // Reporting methods
//    List<MaterialBatchDto> getLowStockBatches(BigDecimal threshold);
//    List<MaterialBatchDto> getBatchesByDateRange(LocalDate startDate, LocalDate endDate);
//    BigDecimal calculateTotalValue(); // Total inventory value
//    BigDecimal calculateTotalValueByMaterial(Long materialId);
//
//    // Quality control
//    List<MaterialBatchDto> getPendingTestBatches();
//    List<MaterialBatchDto> getFailedTestBatches();
//    void approveTestBatch(Long batchId, String testResults);
//    void rejectTestBatch(Long batchId, String rejectionReason);
}
