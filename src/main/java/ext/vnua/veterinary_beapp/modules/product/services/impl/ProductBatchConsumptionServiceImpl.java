package ext.vnua.veterinary_beapp.modules.product.services.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchRepository;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductBatchConsumptionDto;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductBatchConsumptionMapper;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBatch;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBatchConsumption;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductBatchConsumptionRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductBatchRepository;
import ext.vnua.veterinary_beapp.modules.product.services.ProductBatchConsumptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductBatchConsumptionServiceImpl implements ProductBatchConsumptionService {
    private final ProductBatchConsumptionRepository repo;
    private final ProductBatchRepository batchRepo;
    private final MaterialBatchRepository materialRepo;
    private final ProductBatchConsumptionMapper mapper;

    @Override
    public List<ProductBatchConsumptionDto> getByBatch(Long batchId) {
        return repo.findByProductBatchId(batchId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductBatchConsumption", description = "Tạo mới Consumption cho ProductBatch")
    public void reserveConsumption(Long batchId, Long materialBatchId, BigDecimal plannedQty) {
        // TODO: Refactor to work with MaterialBatchItem instead of MaterialBatch
        // MaterialBatch is now just a container, actual inventory is in MaterialBatchItem
        // Need to:
        // 1. Accept materialBatchItemId instead of materialBatchId
        // 2. Query MaterialBatchItem and check availableQuantity
        // 3. Update MaterialBatchItem.reservedQuantity and availableQuantity
        // 4. Link ProductBatchConsumption to MaterialBatchItem
        throw new UnsupportedOperationException(
                "This method needs refactoring to work with MaterialBatchItem. " +
                "Quantities (available, reserved, current) are now tracked per MaterialBatchItem, not MaterialBatch.");
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductBatchConsumption", description = "Hoàn tất Consumption cho ProductBatch")
    public void completeConsumption(Long batchId) {
        // TODO: Refactor to work with MaterialBatchItem instead of MaterialBatch
        // Need to:
        // 1. Get MaterialBatchItem from each ProductBatchConsumption
        // 2. Update MaterialBatchItem.reservedQuantity and currentQuantity
        // 3. Use MaterialBatchItem.reserve() and MaterialBatchItem.updateQuantity() methods
        throw new UnsupportedOperationException(
                "This method needs refactoring to work with MaterialBatchItem. " +
                "ProductBatchConsumption now references MaterialBatchItem, not MaterialBatch. " +
                "Use item.reserve() and item.updateQuantity() methods.");
    }
}
