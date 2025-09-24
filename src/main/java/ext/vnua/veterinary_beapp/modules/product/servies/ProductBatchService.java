package ext.vnua.veterinary_beapp.modules.product.servies;

import ext.vnua.veterinary_beapp.modules.product.dto.ProductBatchDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.CompleteBatchRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.IssueBatchRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.SimulateConsumptionRequest;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBatch;
import ext.vnua.veterinary_beapp.modules.product.repository.custom.CustomProductBatchQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductBatchService {
    Page<ProductBatch> getAllBatches(CustomProductBatchQuery.ProductBatchFilterParam param, PageRequest pageRequest);
    ProductBatchDto getById(Long id);
    ProductBatchDto getByBatchNumber(String batchNumber);

    // Planning & simulation
    Map<Long, BigDecimal> calculateMaterialNeeds(Long productId, BigDecimal plannedQty, Long formulaId);
    ConsumptionPlan simulateConsumption(SimulateConsumptionRequest request); // FIFO + thiếu NVL

    // Lifecycle
    ProductBatchDto issueBatch(IssueBatchRequest request);     // phát hành lệnh, reserve NVL, gen batch code
    ProductBatchDto completeBatch(CompleteBatchRequest request); // trừ NVL, nhập TP, tính hiệu suất
    void closeBatch(Long batchId);

    // Stock ops
    void adjustBatchCurrentStock(Long batchId, BigDecimal delta); // nếu cần
    List<ProductBatchDto> deleteBatches(List<Long> ids);

    // Helper record
    record MaterialPick(Long materialBatchId, BigDecimal quantity) {}
    record Shortage(Long materialId, BigDecimal shortageQty) {}
    record ConsumptionPlan(List<MaterialPick> picks, List<Shortage> shortages) {}
}