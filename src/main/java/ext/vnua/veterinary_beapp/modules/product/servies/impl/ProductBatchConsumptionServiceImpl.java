package ext.vnua.veterinary_beapp.modules.product.servies.impl;

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
import ext.vnua.veterinary_beapp.modules.product.servies.ProductBatchConsumptionService;
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
        ProductBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new DataExistException("Batch không tồn tại"));
        MaterialBatch mb = materialRepo.findById(materialBatchId)
                .orElseThrow(() -> new DataExistException("MaterialBatch không tồn tại"));

        if (mb.getAvailableQuantity().compareTo(plannedQty) < 0) {
            throw new IllegalStateException("Không đủ NVL để reserve");
        }

        // Trừ available, tăng reserved
        mb.setAvailableQuantity(mb.getAvailableQuantity().subtract(plannedQty));
        mb.setReservedQuantity(mb.getReservedQuantity().add(plannedQty));
        materialRepo.save(mb);

        // Ghi consumption
        ProductBatchConsumption c = new ProductBatchConsumption();
        c.setProductBatch(batch);
        c.setMaterialBatch(mb);
        c.setPlannedQuantity(plannedQty);
        c.setActualQuantity(BigDecimal.ZERO);
        repo.save(c);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductBatchConsumption", description = "Hoàn tất Consumption cho ProductBatch")
    public void completeConsumption(Long batchId) {
        List<ProductBatchConsumption> list = repo.findByProductBatchId(batchId);
        for (ProductBatchConsumption c : list) {
            MaterialBatch mb = c.getMaterialBatch();

            // Khi complete: trừ reserved → actual
            BigDecimal planned = c.getPlannedQuantity();
            BigDecimal actual = planned; // mặc định dùng = planned, sau có thể cho nhập số actual riêng

            if (mb.getReservedQuantity().compareTo(actual) < 0) {
                throw new IllegalStateException("Reserved NVL không đủ để deduct");
            }

            mb.setReservedQuantity(mb.getReservedQuantity().subtract(actual));
            mb.setCurrentQuantity(mb.getCurrentQuantity().subtract(actual));
            materialRepo.save(mb);

            c.setActualQuantity(actual);
            repo.save(c);
        }
    }
}
