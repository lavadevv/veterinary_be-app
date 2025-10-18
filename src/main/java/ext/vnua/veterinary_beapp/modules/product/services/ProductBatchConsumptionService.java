package ext.vnua.veterinary_beapp.modules.product.services;


import ext.vnua.veterinary_beapp.modules.product.dto.ProductBatchConsumptionDto;

import java.math.BigDecimal;
import java.util.List;

public interface ProductBatchConsumptionService {
    List<ProductBatchConsumptionDto> getByBatch(Long batchId);

    // Khi issue: ghi planned consumption
    void reserveConsumption(Long batchId, Long materialBatchId, BigDecimal plannedQty);

    // Khi complete: update actual consumption + deduct từ MaterialBatch
    void completeConsumption(Long batchId);
}