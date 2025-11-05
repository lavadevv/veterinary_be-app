package ext.vnua.veterinary_beapp.modules.production.services.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchRepository;
import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderMaterialDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.UpdateProductionOrderMaterialRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.materials.CreateProductionOrderMaterialRequest;
import ext.vnua.veterinary_beapp.modules.production.enums.ProductionOrderStatus;
import ext.vnua.veterinary_beapp.modules.production.mapper.ProductionOrderMaterialMapper;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrderMaterial;
import ext.vnua.veterinary_beapp.modules.production.repository.ProductionOrderMaterialRepository;
import ext.vnua.veterinary_beapp.modules.production.repository.ProductionOrderRepository;
import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionOrderMaterialQuery;
import ext.vnua.veterinary_beapp.modules.production.services.ProductionOrderMaterialService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionOrderMaterialServiceImpl implements ProductionOrderMaterialService {

    private final ProductionOrderMaterialRepository materialRepo;
    private final ProductionOrderRepository orderRepo;
    private final MaterialBatchRepository batchRepo;
    private final ProductionOrderMaterialMapper materialMapper;

    // Constants for material status
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ISSUED = "ISSUED";
    private static final String STATUS_USED = "USED";
    private static final String STATUS_RETURNED = "RETURNED";

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductionOrderMaterial", description = "Thêm nguyên liệu vào lệnh sản xuất")
    public ProductionOrderMaterialDto createMaterial(CreateProductionOrderMaterialRequest req) {
        log.info("Creating production order material for order: {} and batch: {}",
                req.getProductionOrderId(), req.getMaterialBatchId());

        // Validate input
        validateCreateRequest(req);

        // Check if production order exists and is valid
        ProductionOrder order = orderRepo.findById(req.getProductionOrderId())
                .orElseThrow(() -> new DataExistException("Lệnh sản xuất không tồn tại với ID: " + req.getProductionOrderId()));

        // Validate order status
        validateOrderStatusForMaterialAddition(order);

        // Check if material batch exists and validate availability
        MaterialBatch batch = batchRepo.findById(req.getMaterialBatchId())
                .orElseThrow(() -> new DataExistException("Lô nguyên liệu không tồn tại với ID: " + req.getMaterialBatchId()));

        // Validate batch availability
        validateBatchForProduction(batch, req.getRequiredQuantity());

        // Check for duplicate material batch in same production order
        checkDuplicateMaterialBatch(req.getProductionOrderId(), req.getMaterialBatchId());

        // Create new production order material
        ProductionOrderMaterial material = buildProductionOrderMaterial(order, batch, req);

        ProductionOrderMaterial savedMaterial = materialRepo.save(material);

        // Update batch reserved quantity if needed
        updateBatchReservedQuantity(batch, req.getRequiredQuantity());

        log.info("Successfully created production order material with ID: {}", savedMaterial.getId());

        return materialMapper.toDto(savedMaterial);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrderMaterial", description = "Cập nhật nguyên liệu trong lệnh sản xuất")
    public ProductionOrderMaterialDto  updateMaterial(UpdateProductionOrderMaterialRequest req) {
        log.info("Updating production order material with ID: {}", req.getId());

        ProductionOrderMaterial material = materialRepo.findById(req.getId())
                .orElseThrow(() -> new DataExistException("Chi tiết nguyên liệu không tồn tại với ID: " + req.getId()));

        // Store old values for validation
        BigDecimal oldIssuedQuantity = material.getIssuedQuantity();
        String oldStatus = material.getStatus();

        // Validate update permissions based on current status and order status
        validateUpdatePermissions(material, req);

        // Update fields with validation
        updateMaterialFields(material, req);

        // Handle batch quantity updates if issued quantity changed
        if (req.getIssuedQuantity() != null &&
                !req.getIssuedQuantity().equals(oldIssuedQuantity)) {
            handleBatchQuantityUpdate(material, oldIssuedQuantity, req.getIssuedQuantity());
        }

        ProductionOrderMaterial updatedMaterial = materialRepo.save(material);
        log.info("Successfully updated production order material with ID: {}", updatedMaterial.getId());

        return materialMapper.toDto(updatedMaterial);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "ProductionOrderMaterial", description = "Xóa nguyên liệu khỏi lệnh sản xuất")
    public void deleteMaterial(Long id) {
        log.info("Deleting production order material with ID: {}", id);

        ProductionOrderMaterial material = materialRepo.findById(id)
                .orElseThrow(() -> new DataExistException("Chi tiết nguyên liệu không tồn tại với ID: " + id));

        // Validate deletion permissions
        validateDeletionPermissions(material);

        // Release reserved quantity if needed
        if (STATUS_PENDING.equals(material.getStatus())) {
            releaseReservedQuantity(material.getMaterialBatchItem(), material.getRequiredQuantity());
        }

        materialRepo.delete(material);
        log.info("Successfully deleted production order material with ID: {}", id);
    }

    @Override
    public ProductionOrderMaterialDto getById(Long id) {
        log.debug("Retrieving production order material with ID: {}", id);

        return materialMapper.toDto(materialRepo.findById(id)
                .orElseThrow(() -> new DataExistException("Chi tiết nguyên liệu không tồn tại với ID: " + id)));
    }

    @Override
    public List<ProductionOrderMaterialDto> getByOrder(Long orderId) {
        log.debug("Retrieving all materials for production order: {}", orderId);

        // Validate order exists
        if (!orderRepo.existsById(orderId)) {
            throw new DataExistException("Lệnh sản xuất không tồn tại với ID: " + orderId);
        }

        List<ProductionOrderMaterial> materials = materialRepo.findAll((root, query, cb) ->
                cb.equal(root.join("productionOrder").get("id"), orderId));

        return materials.stream()
                .map(materialMapper::toDto)
                .toList();
    }

    @Override
    public Page<ProductionOrderMaterialDto> searchMaterials(Object filterParam, Pageable pageable) {
        log.debug("Searching materials with filter: {}", filterParam);

        Specification<ProductionOrderMaterial> spec =
                CustomProductionOrderMaterialQuery.getFilter(
                        (CustomProductionOrderMaterialQuery.ProductionOrderMaterialFilterParam) filterParam);

        return materialRepo.findAll(spec, pageable).map(materialMapper::toDto);
    }

    // Additional utility methods

    public List<ProductionOrderMaterialDto> getByOrderAndStatus(Long orderId, String status) {
        log.debug("Retrieving materials for order: {} with status: {}", orderId, status);

        List<ProductionOrderMaterial> materials = materialRepo.findAll((root, query, cb) ->
                cb.and(
                        cb.equal(root.join("productionOrder").get("id"), orderId),
                        cb.equal(root.get("status"), status)
                ));

        return materials.stream()
                .map(materialMapper::toDto)
                .toList();
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrderMaterial", description = "Cập nhật trạng thái nguyên liệu")
    public void updateMaterialStatus(Long id, String status) {
        log.info("Updating status for material ID: {} to status: {}", id, status);

        ProductionOrderMaterial material = materialRepo.findById(id)
                .orElseThrow(() -> new DataExistException("Chi tiết nguyên liệu không tồn tại với ID: " + id));

        validateStatusTransition(material.getStatus(), status);

        material.setStatus(status);
        materialRepo.save(material);

        log.info("Successfully updated material status for ID: {}", id);
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrderMaterial", description = "Cập nhật trạng thái nhiều nguyên liệu")
    public void bulkUpdateMaterialStatus(List<Long> ids, String status) {
        log.info("Bulk updating status for {} materials to status: {}", ids.size(), status);

        List<ProductionOrderMaterial> materials = materialRepo.findAllById(ids);

        if (materials.size() != ids.size()) {
            throw new DataExistException("Một số chi tiết nguyên liệu không tồn tại");
        }

        materials.forEach(material -> {
            validateStatusTransition(material.getStatus(), status);
            material.setStatus(status);
        });

        materialRepo.saveAll(materials);
        log.info("Successfully bulk updated {} materials status", materials.size());
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrderMaterial", description = "Cấp phát nguyên liệu cho lệnh sản xuất")
    public void issueMaterials(Long orderId, List<Long> materialIds) {
        log.info("Issuing materials for order: {}", orderId);

        List<ProductionOrderMaterial> materials = materialRepo.findAllById(materialIds);

        materials.forEach(material -> {
            if (!STATUS_PENDING.equals(material.getStatus())) {
                throw new DataExistException("Chỉ có thể cấp phát nguyên liệu ở trạng thái PENDING");
            }

            if (!material.getProductionOrder().getId().equals(orderId)) {
                throw new DataExistException("Nguyên liệu không thuộc lệnh sản xuất này");
            }

            // Set issued quantity to required quantity if not set
            if (material.getIssuedQuantity() == null) {
                material.setIssuedQuantity(material.getRequiredQuantity());
            }

            material.setStatus(STATUS_ISSUED);
        });

        materialRepo.saveAll(materials);
        log.info("Successfully issued {} materials", materials.size());
    }

    @Override
    public BigDecimal getTotalRequiredQuantityByMaterial(Long materialId) {
        List<ProductionOrderMaterial> list = materialRepo.findAll((root, query, cb) ->
                cb.equal(root.join("materialBatch").join("material").get("id"), materialId)
        );

        return list.stream()
                .map(ProductionOrderMaterial::getRequiredQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    // Private validation and utility methods

    private void validateCreateRequest(CreateProductionOrderMaterialRequest req) {
        if (req.getProductionOrderId() == null) {
            throw new IllegalArgumentException("Production Order ID không được để trống");
        }
        if (req.getMaterialBatchId() == null) {
            throw new IllegalArgumentException("Material Batch ID không được để trống");
        }
        if (req.getRequiredQuantity() == null || req.getRequiredQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng yêu cầu phải lớn hơn 0");
        }
    }

    private void validateOrderStatusForMaterialAddition(ProductionOrder order) {
        // Only allow adding materials to certain order statuses
        if (order.getStatus() == ProductionOrderStatus.COMPLETED ||
                order.getStatus() == ProductionOrderStatus.CLOSED ||
                order.getStatus() == ProductionOrderStatus.CANCELLED) {
            throw new DataExistException("Không thể thêm nguyên liệu vào lệnh sản xuất có trạng thái: " +
                    order.getStatus().getDisplayName());
        }
    }

    private void validateBatchForProduction(MaterialBatch batch, BigDecimal requiredQuantity) {
        // TODO: REFACTOR - This method needs to be updated for MaterialBatchItem
        // MaterialBatch no longer has direct usageStatus, expiryDate, availableQuantity
        // These properties are now in MaterialBatchItem
        throw new UnsupportedOperationException(
            "Batch validation needs refactoring for MaterialBatchItem structure");
        
        /*
        // Check if batch is available for production
        if (batch.getUsageStatus() != UsageStatus.SAN_SANG_SU_DUNG) {
            throw new DataExistException("Lô nguyên liệu chưa được phép sử dụng");
        }

        // Check expiry date
        if (batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            throw new DataExistException("Lô nguyên liệu đã hết hạn sử dụng");
        }

        // Check expiry date as additional validation
        if (batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            throw new DataExistException("Lô nguyên liệu đã hết hạn sử dụng");
        }

        // Check available quantity
        BigDecimal availableQuantity = batch.getAvailableQuantity() != null ?
                batch.getAvailableQuantity() : batch.getCurrentQuantity();

        if (availableQuantity.compareTo(requiredQuantity) < 0) {
            throw new DataExistException("Lô nguyên liệu không đủ số lượng yêu cầu. " +
                    "Có sẵn: " + availableQuantity + ", Yêu cầu: " + requiredQuantity);
        }
        */
    }

    private void checkDuplicateMaterialBatch(Long orderId, Long batchId) {
        Optional<ProductionOrderMaterial> existing = materialRepo.findOne((root, query, cb) ->
                cb.and(
                        cb.equal(root.join("productionOrder").get("id"), orderId),
                        cb.equal(root.join("materialBatch").get("id"), batchId)
                ));

        if (existing.isPresent()) {
            throw new DataExistException("Lô nguyên liệu này đã được thêm vào lệnh sản xuất");
        }
    }

    private ProductionOrderMaterial buildProductionOrderMaterial(ProductionOrder order, MaterialBatch batch,
                                                                 CreateProductionOrderMaterialRequest req) {
        // TODO: REFACTOR - This method needs MaterialBatchItem instead of MaterialBatch
        throw new UnsupportedOperationException(
            "Building ProductionOrderMaterial needs refactoring to use MaterialBatchItem");
            
        /*
        ProductionOrderMaterial material = new ProductionOrderMaterial();
        material.setProductionOrder(order);
        material.setMaterialBatchItem(batchItem);  // Changed from setMaterialBatch
        material.setRequiredQuantity(req.getRequiredQuantity());
        material.setIssuedQuantity(null);
        material.setActualQuantity(null);
        material.setStatus(STATUS_PENDING);
        material.setNotes(req.getNotes());

        return material;
        */
    }

    private void updateBatchReservedQuantity(MaterialBatch batch, BigDecimal quantity) {
        // DEPRECATED: MaterialBatch version - throws exception
        throw new UnsupportedOperationException(
            "Reserved quantity management needs refactoring for MaterialBatchItem. " +
            "Use updateBatchReservedQuantity(MaterialBatchItem, BigDecimal) instead.");
    }
    
    // New method for MaterialBatchItem
    private void updateBatchReservedQuantity(ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItem batchItem, BigDecimal quantity) {
        if (batchItem.getReservedQuantity() == null) {
            batchItem.setReservedQuantity(BigDecimal.ZERO);
        }
        batchItem.setReservedQuantity(batchItem.getReservedQuantity().add(quantity));

        // Update available quantity
        updateBatchAvailableQuantity(batchItem);
        // Note: Need MaterialBatchItemRepository to save
        // batchItemRepo.save(batchItem);
    }

    private void releaseReservedQuantity(MaterialBatch batch, BigDecimal quantity) {
        // DEPRECATED: MaterialBatch version - throws exception
        throw new UnsupportedOperationException(
            "Reserved quantity management needs refactoring for MaterialBatchItem. " +
            "Use releaseReservedQuantity(MaterialBatchItem, BigDecimal) instead.");
    }
    
    // New method for MaterialBatchItem
    private void releaseReservedQuantity(ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItem batchItem, BigDecimal quantity) {
        if (batchItem.getReservedQuantity() != null) {
            batchItem.setReservedQuantity(batchItem.getReservedQuantity().subtract(quantity));
            if (batchItem.getReservedQuantity().compareTo(BigDecimal.ZERO) < 0) {
                batchItem.setReservedQuantity(BigDecimal.ZERO);
            }
        }

        // Update available quantity
        updateBatchAvailableQuantity(batchItem);
        // Note: Need MaterialBatchItemRepository to save
        // batchItemRepo.save(batchItem);
    }

    private void updateBatchAvailableQuantity(MaterialBatch batch) {
        // DEPRECATED: MaterialBatch version - throws exception
        throw new UnsupportedOperationException(
            "Available quantity calculation needs refactoring for MaterialBatchItem. " +
            "Use updateBatchAvailableQuantity(MaterialBatchItem) instead.");
    }
    
    // New method for MaterialBatchItem
    private void updateBatchAvailableQuantity(ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItem batchItem) {
        BigDecimal reservedQty = batchItem.getReservedQuantity() != null ?
                batchItem.getReservedQuantity() : BigDecimal.ZERO;
        batchItem.setAvailableQuantity(batchItem.getCurrentQuantity().subtract(reservedQty));
    }

    private void validateUpdatePermissions(ProductionOrderMaterial material, UpdateProductionOrderMaterialRequest req) {
        // Check order status
        ProductionOrderStatus orderStatus = material.getProductionOrder().getStatus();
        if (orderStatus == ProductionOrderStatus.COMPLETED ||
                orderStatus == ProductionOrderStatus.CLOSED ||
                orderStatus == ProductionOrderStatus.CANCELLED) {
            throw new DataExistException("Không thể cập nhật nguyên liệu của lệnh sản xuất có trạng thái: " +
                    orderStatus.getDisplayName());
        }

        // Check material status for specific updates
        String currentStatus = material.getStatus();
        if (STATUS_USED.equals(currentStatus) && req.getIssuedQuantity() != null) {
            throw new DataExistException("Không thể thay đổi số lượng cấp phát của nguyên liệu đã sử dụng");
        }
    }

    private void updateMaterialFields(ProductionOrderMaterial material, UpdateProductionOrderMaterialRequest req) {
        if (req.getIssuedQuantity() != null) {
            validateQuantity(req.getIssuedQuantity(), "Số lượng cấp phát");

            // Validate issued quantity against required quantity
            if (req.getIssuedQuantity().compareTo(material.getRequiredQuantity()) > 0) {
                throw new IllegalArgumentException("Số lượng cấp phát không được vượt quá số lượng yêu cầu");
            }

            material.setIssuedQuantity(req.getIssuedQuantity());
        }

        if (req.getActualQuantity() != null) {
            validateQuantity(req.getActualQuantity(), "Số lượng thực tế");
            material.setActualQuantity(req.getActualQuantity());
        }

        if (StringUtils.hasText(req.getStatus())) {
            validateStatusTransition(material.getStatus(), req.getStatus());
            material.setStatus(req.getStatus());
        }

        if (req.getNotes() != null) {
            material.setNotes(req.getNotes());
        }
    }

    private void handleBatchQuantityUpdate(ProductionOrderMaterial material,
                                           BigDecimal oldIssuedQuantity, BigDecimal newIssuedQuantity) {
        var batchItem = material.getMaterialBatchItem();

        if (oldIssuedQuantity != null) {
            // Release old reserved quantity
            releaseReservedQuantity(batchItem, oldIssuedQuantity);
        }

        // Reserve new quantity
        updateBatchReservedQuantity(batchItem, newIssuedQuantity);
    }

    private void validateQuantity(BigDecimal quantity, String fieldName) {
        if (quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " không được âm");
        }
    }

    private void validateDeletionPermissions(ProductionOrderMaterial material) {
        // Can only delete pending materials
        if (!STATUS_PENDING.equals(material.getStatus())) {
            throw new DataExistException("Chỉ có thể xóa chi tiết nguyên liệu ở trạng thái PENDING");
        }

        // Check order status
        ProductionOrderStatus orderStatus = material.getProductionOrder().getStatus();
        if (orderStatus == ProductionOrderStatus.IN_PROGRESS ||
                orderStatus == ProductionOrderStatus.QC_PENDING ||
                orderStatus == ProductionOrderStatus.COMPLETED ||
                orderStatus == ProductionOrderStatus.CLOSED) {
            throw new DataExistException("Không thể xóa nguyên liệu khi lệnh sản xuất đang/đã thực hiện");
        }
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Define valid status transitions based on production workflow
        switch (currentStatus) {
            case STATUS_PENDING:
                if (!List.of(STATUS_ISSUED, STATUS_RETURNED).contains(newStatus)) {
                    throw new DataExistException("Không thể chuyển từ trạng thái " + currentStatus + " sang " + newStatus);
                }
                break;
            case STATUS_ISSUED:
                if (!List.of(STATUS_USED, STATUS_RETURNED).contains(newStatus)) {
                    throw new DataExistException("Không thể chuyển từ trạng thái " + currentStatus + " sang " + newStatus);
                }
                break;
            case STATUS_USED:
                // Used materials cannot change status
                throw new DataExistException("Không thể thay đổi trạng thái của nguyên liệu đã sử dụng");
            case STATUS_RETURNED:
                if (!List.of(STATUS_ISSUED).contains(newStatus)) {
                    throw new DataExistException("Nguyên liệu đã trả về chỉ có thể cấp phát lại");
                }
                break;
            default:
                throw new DataExistException("Trạng thái không hợp lệ: " + currentStatus);
        }
    }
}