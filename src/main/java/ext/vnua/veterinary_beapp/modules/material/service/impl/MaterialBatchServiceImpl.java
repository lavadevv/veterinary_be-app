package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.DataNotFoundException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchDto;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchItemDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.BatchQuantityAdjustmentRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.BatchTransferRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateMaterialBatchContainerRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateMaterialBatchItemRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateMaterialBatchRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.UpdateMaterialBatchRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.response.MaterialBatchDetailDTO;
import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import ext.vnua.veterinary_beapp.modules.material.mapper.MaterialBatchMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItem;
import ext.vnua.veterinary_beapp.modules.material.model.ActiveIngredient;
import ext.vnua.veterinary_beapp.modules.material.model.Supplier;
import ext.vnua.veterinary_beapp.modules.material.model.Manufacturer;
import ext.vnua.veterinary_beapp.modules.material.repository.LocationRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.SupplierRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.ManufacturerRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.ActiveIngredientRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialBatchQuery;
import ext.vnua.veterinary_beapp.modules.material.service.LocationCapacityService;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialBatchService;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialService;
import ext.vnua.veterinary_beapp.modules.material.events.StockSyncPublisher;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialBatchServiceImpl implements MaterialBatchService {

    private final MaterialBatchRepository materialBatchRepository;
    private final MaterialRepository materialRepository;
    private final LocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ActiveIngredientRepository activeIngredientRepository;
    private final MaterialService materialService;
    private final MaterialBatchMapper materialBatchMapper;
    private final StockSyncPublisher stockSyncPublisher;
    private final LocationCapacityService locationCapacityService;

    @Override
    public Page<MaterialBatchDto> getAllMaterialBatch(CustomMaterialBatchQuery.MaterialBatchFilterParam param,
                                                      PageRequest pageRequest) {
        Specification<MaterialBatch> specification = CustomMaterialBatchQuery.getFilterMaterialBatch(param);
        return materialBatchRepository.findAll(specification, pageRequest)
                .map(materialBatchMapper::toMaterialBatchDto);  // Removed enrichWithActiveIngredients
    }

    @Override
    public MaterialBatchDto selectMaterialBatchById(Long id) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findById(id);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Lô vật liệu không tồn tại");
        }
        MaterialBatch materialBatch = materialBatchOptional.get();
        return materialBatchMapper.toMaterialBatchDto(materialBatch);
    }

    @Override
    public MaterialBatchDto selectMaterialBatchByBatchNumber(String batchNumber) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findByBatchNumber(batchNumber);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Số lô không tồn tại");
        }
        MaterialBatch materialBatch = materialBatchOptional.get();
        return materialBatchMapper.toMaterialBatchDto(materialBatch);
    }

    @Override
    public MaterialBatchDto selectMaterialBatchByInternalCode(String internalCode) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findByInternalBatchCode(internalCode);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Mã lô nội bộ không tồn tại");
        }
        MaterialBatch materialBatch = materialBatchOptional.get();
        return materialBatchMapper.toMaterialBatchDto(materialBatch);
    }

    @Override
    public List<MaterialBatchDto> selectMaterialBatchesByMaterial(Long materialId) {
        // TODO: Refactor - MaterialBatch no longer has direct material field
        // Query should be done on MaterialBatchItem: "find batches that contain items for this material"
        throw new UnsupportedOperationException(
                "Query by material needs refactoring. " +
                "Use MaterialBatchItemRepository to find items by material, then get their batches.");
    }

    @Override
    public List<MaterialBatchDto> selectMaterialBatchesByLocation(Long locationId) {
        List<MaterialBatch> materialBatches = materialBatchRepository.findByLocationId(locationId);
        return materialBatches.stream()
                .map(materialBatchMapper::toMaterialBatchDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "MaterialBatch", description = "Tạo mới lô vật liệu")
    public MaterialBatchDto createMaterialBatch(CreateMaterialBatchRequest request) {
        // TODO: Refactor to create MaterialBatch with MaterialBatchItems
        // MaterialBatch no longer has direct material/quantity fields
        // Need to create MaterialBatchItems for each material in the shipment
        throw new UnsupportedOperationException(
            "This method needs refactoring to support MaterialBatchItem structure. " +
            "MaterialBatch is now a container - create MaterialBatchItems for individual materials.");
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "MaterialBatch", description = "Tạo mới lô container")
    public MaterialBatchDto createMaterialBatchContainer(CreateMaterialBatchContainerRequest request) {
        // Validate unique batch number
        if (request.getBatchNumber() != null && 
            materialBatchRepository.existsByBatchNumber(request.getBatchNumber())) {
            throw new DataExistException("Số lô đã tồn tại: " + request.getBatchNumber());
        }

        // Validate unique internal code if provided
        if (request.getInternalBatchCode() != null && !request.getInternalBatchCode().isBlank() &&
            materialBatchRepository.existsByInternalBatchCode(request.getInternalBatchCode())) {
            throw new DataExistException("Mã lô nội bộ đã tồn tại: " + request.getInternalBatchCode());
        }

        // Validate supplier exists
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new DataNotFoundException("Nhà cung cấp không tồn tại với ID: " + request.getSupplierId()));

        // Validate manufacturer exists
        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new DataNotFoundException("Nhà sản xuất không tồn tại với ID: " + request.getManufacturerId()));

        // Validate location if provided
        Location location = null;
        if (request.getLocationId() != null) {
            location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new DataNotFoundException("Vị trí không tồn tại với ID: " + request.getLocationId()));
        }

        // Create batch container
        MaterialBatch batch = new MaterialBatch();
        batch.setBatchNumber(request.getBatchNumber());
        batch.setInternalBatchCode(request.getInternalBatchCode());
        batch.setReceivedDate(request.getReceivedDate());
        batch.setInvoiceNumber(request.getInvoiceNumber());
        batch.setCountryOfOrigin(request.getCountryOfOrigin());
        batch.setBatchStatus(request.getBatchStatus());
        batch.setSupplier(supplier);
        batch.setManufacturer(manufacturer);
        batch.setLocation(location);
        batch.setNotes(request.getNotes());
        
        // Initialize empty items list
        batch.setBatchItems(new ArrayList<>());

        MaterialBatch savedBatch = materialBatchRepository.save(batch);

        return materialBatchMapper.toMaterialBatchDto(savedBatch);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "MaterialBatchItem", description = "Thêm vật liệu vào lô")
    public MaterialBatchDto addItemToBatch(Long batchId, CreateMaterialBatchItemRequest request) {
        // Validate batch exists
        MaterialBatch batch = materialBatchRepository.findById(batchId)
                .orElseThrow(() -> new DataNotFoundException("Lô vật liệu không tồn tại với ID: " + batchId));

        // Validate material exists
        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new DataNotFoundException("Vật liệu không tồn tại với ID: " + request.getMaterialId()));

        // Validate location if provided
        Location itemLocation = null;
        if (request.getLocationId() != null) {
            itemLocation = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new DataNotFoundException("Vị trí không tồn tại với ID: " + request.getLocationId()));
        } else {
            // Use batch's default location if item doesn't specify
            itemLocation = batch.getLocation();
        }

        // Create MaterialBatchItem
        MaterialBatchItem item = new MaterialBatchItem();
        item.setBatch(batch);
        item.setMaterial(material);
        item.setLocation(itemLocation);
        item.setManufacturingDate(request.getManufacturingDate());
        item.setExpiryDate(request.getExpiryDate());
        item.setReceivedQuantity(request.getReceivedQuantity());
        
        // Set current quantity (default to received quantity if not specified)
        if (request.getCurrentQuantity() != null) {
            item.setCurrentQuantity(request.getCurrentQuantity());
        } else {
            item.setCurrentQuantity(request.getReceivedQuantity());
        }
        
        item.setUnitPrice(request.getUnitPrice());
        item.setTaxPercent(request.getTaxPercent());
        item.setSubtotalAmount(request.getSubtotalAmount());
        item.setTaxAmount(request.getTaxAmount());
        item.setTotalAmount(request.getTotalAmount());
        
        // Parse and set status enums
        if (request.getTestStatus() != null) {
            try {
                item.setTestStatus(TestStatus.valueOf(request.getTestStatus()));
            } catch (IllegalArgumentException e) {
                throw new MyCustomException("Trạng thái kiểm nghiệm không hợp lệ: " + request.getTestStatus());
            }
        } else {
            item.setTestStatus(TestStatus.CHO_KIEM_NGHIEM);
        }
        
        if (request.getUsageStatus() != null) {
            try {
                item.setUsageStatus(UsageStatus.valueOf(request.getUsageStatus()));
            } catch (IllegalArgumentException e) {
                throw new MyCustomException("Trạng thái sử dụng không hợp lệ: " + request.getUsageStatus());
            }
        } else {
            item.setUsageStatus(UsageStatus.CACH_LY);
        }
        
        item.setNotes(request.getNotes());
        
        // Add item to batch
        batch.addBatchItem(item);
        
        // Save batch (cascades to items)
        MaterialBatch savedBatch = materialBatchRepository.save(batch);
        
        return materialBatchMapper.toMaterialBatchDto(savedBatch);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialBatch", description = "Cập nhật lô vật liệu")
    public MaterialBatchDto updateMaterialBatch(UpdateMaterialBatchRequest request) {
        // TODO: Refactor to update MaterialBatch with MaterialBatchItems
        // MaterialBatch no longer has direct material/quantity/price fields
        // These fields are now in MaterialBatchItem
        throw new UnsupportedOperationException(
            "This method needs refactoring to support MaterialBatchItem structure. " +
            "Update individual MaterialBatchItems instead of the container.");
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "MaterialBatch", description = "Xóa lô vật liệu")
    public void deleteMaterialBatch(Long id) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findById(id);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Lô vật liệu không tồn tại");
        }

        MaterialBatch materialBatch = materialBatchOptional.get();

        // TODO: Check if batch has any items with positive quantities
        // For now, just delete the container
        materialBatchRepository.deleteById(id);
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "MaterialBatch", description = "Xóa danh sách lô vật liệu")
    public List<MaterialBatchDto> deleteAllIdMaterialBatches(List<Long> ids) {
        List<MaterialBatchDto> materialBatchDtos = new ArrayList<>();
        for (Long id : ids) {
            Optional<MaterialBatch> optionalMaterialBatch = materialBatchRepository.findById(id);
            if (optionalMaterialBatch.isPresent()) {
                MaterialBatch materialBatch = optionalMaterialBatch.get();
                materialBatchDtos.add(materialBatchMapper.toMaterialBatchDto(materialBatch));
                
                // TODO: Check if batch has any items with positive quantities
                materialBatchRepository.delete(materialBatch);
            } else {
                throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách lô vật liệu!");
            }
        }
        return materialBatchDtos;
    }

    @Override
    @Transactional
    public void updateQuantity(Long batchId, BigDecimal newQuantity) {
        // TODO: Refactor - MaterialBatch no longer has currentQuantity field
        // Quantity is now tracked per MaterialBatchItem
        throw new UnsupportedOperationException(
            "Quantity is now tracked per MaterialBatchItem. Use MaterialBatchItemService instead.");
    }

    @Override
    @Transactional
    public void updateTestStatus(Long batchId, String testStatus) {
        // TODO: Refactor - TestStatus is now on MaterialBatchItem
        throw new UnsupportedOperationException(
            "Test status is now tracked per MaterialBatchItem. Use MaterialBatchItemService instead.");
    }

    @Override
    @Transactional
    public void updateUsageStatus(Long batchId, String usageStatus) {
        // TODO: Refactor - UsageStatus is now on MaterialBatchItem
        throw new UnsupportedOperationException(
            "Usage status is now tracked per MaterialBatchItem. Use MaterialBatchItemService instead.");
    }

    @Override
    @Transactional
    public void moveToLocation(Long batchId, Long newLocationId) {
        // Moving entire batch to new location is still valid
        // But location can also be set per MaterialBatchItem
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findById(batchId);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Lô vật liệu không tồn tại");
        }

        MaterialBatch materialBatch = materialBatchOptional.get();
        Location newLocation = null;
        if (newLocationId != null) {
            newLocation = locationRepository.findById(newLocationId)
                    .orElseThrow(() -> new DataExistException("Vị trí mới không tồn tại"));

            if (Boolean.FALSE.equals(newLocation.getIsAvailable())) {
                throw new MyCustomException("Vị trí mới không khả dụng");
            }
        }

        materialBatch.setLocation(newLocation);
        materialBatchRepository.saveAndFlush(materialBatch);
    }

    @Override
    public List<MaterialBatchDto> getExpiredBatches() {
        // TODO: Refactor - expiryDate is now on MaterialBatchItem
        throw new UnsupportedOperationException(
                "Expired batches query needs refactoring. " +
                "Query MaterialBatchItem.expiryDate instead.");
    }

    @Override
    public List<MaterialBatchDto> getBatchesNearExpiry() {
        // TODO: Refactor - expiryDate is now on MaterialBatchItem
        throw new UnsupportedOperationException(
                "Near expiry query needs refactoring. " +
                "Query MaterialBatchItem.expiryDate instead.");
    }

    @Override
    public List<MaterialBatchDto> getUsableBatches() {
        // TODO: Refactor - testStatus, usageStatus, currentQuantity, expiryDate are now on MaterialBatchItem
        throw new UnsupportedOperationException(
                "Usable batches query needs refactoring. " +
                "Query MaterialBatchItem with usable status and available quantity.");
    }

    @Override
    public BigDecimal getTotalQuantityByMaterial(Long materialId) {
        // TODO: Refactor to sum quantities from MaterialBatchItem
        throw new UnsupportedOperationException(
            "Total quantity should be calculated from MaterialBatchItems. " +
            "Use MaterialBatchItemRepository.calculateTotalAvailableQuantity() instead.");
    }

    @Override
    public List<MaterialBatchDto> getOldestUsableBatches(Long materialId) {
        // TODO: Refactor - material, testStatus, usageStatus, currentQuantity are now on MaterialBatchItem
        throw new UnsupportedOperationException(
                "FIFO query needs refactoring. " +
                "Use MaterialBatchItemRepository.findFIFOItemsForAllocation() instead.");
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialBatch", description = "Chuyển lô vật liệu")
    public List<MaterialBatchDto> transferBatches(BatchTransferRequest request) {
        // TODO: Refactor - should move MaterialBatchItems, not the container
        throw new UnsupportedOperationException(
            "Batch transfer should operate on MaterialBatchItems. " +
            "Implement MaterialBatchItemService.transferItems() instead.");
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialBatch", description = "Điều chỉnh số lượng lô")
    public void adjustBatchQuantity(BatchQuantityAdjustmentRequest request) {
        // TODO: Refactor - quantities are now on MaterialBatchItem
        throw new UnsupportedOperationException(
            "Quantity adjustments should be done on MaterialBatchItems. " +
            "Use MaterialBatchItemService.adjustItemQuantity() instead.");
    }

    @Override
    @Transactional
    public void markBatchAsExpired(Long batchId) {
        // TODO: Refactor - expiry status is now on MaterialBatchItem
        throw new UnsupportedOperationException(
            "Expiry status is tracked per MaterialBatchItem. " +
            "Use MaterialBatchItemService to mark items as expired.");
    }

    @Override
    @Transactional
    public void markBatchAsConsumed(Long batchId) {
        // TODO: Refactor - consumption tracking is now on MaterialBatchItem
        throw new UnsupportedOperationException(
            "Consumption is tracked per MaterialBatchItem. " +
            "Use MaterialBatchItemService to mark items as consumed.");
    }

    @Override
    public List<MaterialBatchDto> getLowStockBatches(BigDecimal threshold) {
        // TODO: Refactor to query MaterialBatchItems with low stock
        throw new UnsupportedOperationException(
            "Low stock should be checked per MaterialBatchItem. " +
            "Use MaterialBatchItemRepository to find items with low currentQuantity.");
    }

    @Override
    public BigDecimal calculateTotalValue() {
        // TODO: Refactor to sum from MaterialBatchItems
        throw new UnsupportedOperationException(
            "Total value should be calculated from MaterialBatchItems. " +
            "Sum (item.currentQuantity * item.unitPrice) for all items.");
    }

    @Override
    public BigDecimal calculateTotalValueByMaterial(Long materialId) {
        // TODO: Refactor to sum from MaterialBatchItems
        throw new UnsupportedOperationException(
            "Material value should be calculated from MaterialBatchItems. " +
            "Query items by material and sum (currentQuantity * unitPrice).");
    }

    @Override
    public List<MaterialBatchDto> getBatchesByDateRange(LocalDate startDate, LocalDate endDate) {
        CustomMaterialBatchQuery.MaterialBatchFilterParam param =
                new CustomMaterialBatchQuery.MaterialBatchFilterParam();
        param.setReceivedFromDate(startDate);
        param.setReceivedToDate(endDate);

        Specification<MaterialBatch> specification = CustomMaterialBatchQuery.getFilterMaterialBatch(param);
        List<MaterialBatch> batches = materialBatchRepository.findAll(specification);

        return batches.stream()
                .map(materialBatchMapper::toMaterialBatchDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MaterialBatchDto> getPendingTestBatches() {
        // TODO: Refactor - test status is now on MaterialBatchItem
        throw new UnsupportedOperationException(
            "Test status is tracked per MaterialBatchItem. " +
            "Query MaterialBatchItems with pending test status.");
    }

    @Override
    public List<MaterialBatchDto> getFailedTestBatches() {
        // TODO: Refactor - test status is now on MaterialBatchItem
        throw new UnsupportedOperationException(
            "Test status is tracked per MaterialBatchItem. " +
            "Query MaterialBatchItems with failed test status.");
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialBatch", description = "Duyệt kiểm nghiệm lô")
    public void approveTestBatch(Long batchId, String testResults) {
        // TODO: Refactor - test approval should be done per MaterialBatchItem
        throw new UnsupportedOperationException(
            "Test approval should be done per MaterialBatchItem. " +
            "Use MaterialBatchItemService to approve individual items.");
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialBatch", description = "Từ chối kiểm nghiệm lô")
    public void rejectTestBatch(Long batchId, String rejectionReason) {
        // TODO: Refactor - test rejection should be done per MaterialBatchItem
        throw new UnsupportedOperationException(
            "Test rejection should be done per MaterialBatchItem. " +
            "Use MaterialBatchItemService to reject individual items.");
    }

    // ========== DEPRECATED: Batch Detail with Active Ingredients ==========
    // These methods need to be refactored for new MaterialBatchItem structure

    @Override
    public MaterialBatchDetailDTO getBatchDetailWithIngredients(Long batchId) {
        throw new UnsupportedOperationException(
            "Method deprecated. Please use MaterialBatchItem endpoints instead.");
    }

    @Override
    public List<MaterialBatchDetailDTO> getAllBatchesWithDetails() {
        throw new UnsupportedOperationException(
            "Method deprecated. Please use MaterialBatchItem endpoints instead.");
    }

    @Override
    public List<MaterialBatchDetailDTO> getBatchesWithUnqualifiedIngredients() {
        throw new UnsupportedOperationException(
            "Method deprecated. Please use MaterialBatchItem endpoints instead.");
    }
    
    // ========== DEPRECATED: Active Ingredients Helper Methods ==========
    // These methods are deprecated. Active ingredients are now managed through MaterialBatchItemActiveIngredient
    
    /*
    private MaterialBatchDto enrichWithActiveIngredients(MaterialBatchDto dto, MaterialBatch entity) {
        if (entity.getBatchActiveIngredients() != null && !entity.getBatchActiveIngredients().isEmpty()) {
            List<MaterialBatchActiveIngredientDTO> ingredientDTOs = entity.getBatchActiveIngredients().stream()
                    .map(this::mapToActiveIngredientDTO)
                    .toList();
            dto.setActiveIngredients(ingredientDTOs);
        }
        return dto;
    }
    
    private MaterialBatchActiveIngredientDTO mapToActiveIngredientDTO(MaterialBatchActiveIngredient ingredient) {
        MaterialBatchActiveIngredientDTO dto = new MaterialBatchActiveIngredientDTO();
        dto.setId(ingredient.getId());
        dto.setActiveIngredientId(ingredient.getActiveIngredient().getId());
        dto.setActiveIngredientName(ingredient.getActiveIngredient().getIngredientName());
        dto.setCoaContent(ingredient.getCoaContent());
        dto.setActualContent(ingredient.getActualContent());
        dto.setRatio(ingredient.calculateKQPTCOARatio());
        dto.setQualificationStatus(ingredient.getQualificationStatus());
        dto.setIsQualified(ingredient.isQualified());
        return dto;
    }
    */
    
    // ========== Helper: Process Active Ingredients from Request ==========
    // TODO: These methods are deprecated - active ingredients are now managed through MaterialBatchItemActiveIngredient
    // Active ingredients should be added to individual MaterialBatchItems, not to the container MaterialBatch
    
    private void processActiveIngredients(MaterialBatch batch, List<?> activeIngredientsInput) {
        // Deprecated - MaterialBatch no longer has direct active ingredients
        // Active ingredients are now linked to MaterialBatchItem
        throw new UnsupportedOperationException(
            "Active ingredients are now managed per MaterialBatchItem. " +
            "Use MaterialBatchItemActiveIngredient instead.");
    }
    
    private void createBatchActiveIngredient(MaterialBatch batch, CreateMaterialBatchRequest.ActiveIngredientInput input) {
        // Deprecated - use MaterialBatchItemActiveIngredient
        throw new UnsupportedOperationException(
            "Active ingredients are now managed per MaterialBatchItem.");
    }
    
    private void createBatchActiveIngredient(MaterialBatch batch, UpdateMaterialBatchRequest.ActiveIngredientInput input) {
        // Deprecated - use MaterialBatchItemActiveIngredient
        throw new UnsupportedOperationException(
            "Active ingredients are now managed per MaterialBatchItem.");
    }
}
