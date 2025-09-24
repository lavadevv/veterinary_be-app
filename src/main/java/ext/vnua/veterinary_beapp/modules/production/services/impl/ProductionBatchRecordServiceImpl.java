package ext.vnua.veterinary_beapp.modules.production.services.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.DataNotFoundException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.production.dto.ProductionBatchRecordDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionBatchRecord.CreateProductionBatchRecordRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionBatchRecord.UpdateProductionBatchRecordRequest;
import ext.vnua.veterinary_beapp.modules.production.enums.ProductionOrderStatus;
import ext.vnua.veterinary_beapp.modules.production.mapper.ProductionBatchRecordMapper;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionBatchRecord;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import ext.vnua.veterinary_beapp.modules.production.repository.ProductionBatchRecordRepository;
import ext.vnua.veterinary_beapp.modules.production.repository.ProductionOrderRepository;
import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionBatchRecordQuery;
import ext.vnua.veterinary_beapp.modules.production.services.ProductionBatchRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionBatchRecordServiceImpl implements ProductionBatchRecordService {

    private final ProductionBatchRecordRepository recordRepo;
    private final ProductionOrderRepository orderRepo;
    private final ProductionBatchRecordMapper mapper;

    // Constants for error messages
    private static final String RECORD_NOT_FOUND = "Hồ sơ lô sản xuất không tồn tại";
    private static final String ORDER_NOT_FOUND = "Lệnh sản xuất không tồn tại";
    private static final String CREATE_ERROR = "Có lỗi xảy ra khi tạo hồ sơ lô sản xuất";
    private static final String UPDATE_ERROR = "Có lỗi xảy ra khi cập nhật hồ sơ lô sản xuất";
    private static final String DELETE_ERROR = "Có lỗi xảy ra khi xóa hồ sơ lô sản xuất";

    @Override
    public Page<ProductionBatchRecordDto> getAll(CustomProductionBatchRecordQuery.ProductionBatchRecordFilterParam param, PageRequest pr) {
        try {
            Specification<ProductionBatchRecord> spec = CustomProductionBatchRecordQuery.getFilter(param);
            return recordRepo.findAll(spec, pr).map(mapper::toDto);
        } catch (Exception e) {
            log.error("Error fetching production batch records with filter: {}", param, e);
            throw new MyCustomException("Có lỗi xảy ra khi tải danh sách hồ sơ lô sản xuất");
        }
    }

    @Override
    public ProductionBatchRecordDto getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID không được null");
        }

        ProductionBatchRecord record = findRecordById(id);
        return mapper.toDto(record);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductionBatchRecord", description = "Tạo mới hồ sơ lô sản xuất")
    public ProductionBatchRecordDto create(CreateProductionBatchRecordRequest req) {
        validateCreateRequest(req);

        // Validate production order exists and is active
        ProductionOrder order = validateAndGetProductionOrder(req.getProductionOrderId());

        // Validate business rules
        validateBusinessRules(req, order);

        ProductionBatchRecord entity = mapper.toCreate(req);
        entity.setProductionOrder(order);

        // Set default sequence number if not provided
        if (entity.getSequenceNumber() == null) {
            entity.setSequenceNumber(getNextSequenceNumber(req.getProductionOrderId(), req.getStepName()));
        }

        try {
            ProductionBatchRecord saved = recordRepo.saveAndFlush(entity);
            log.info("Created production batch record with ID: {} for order: {}", saved.getId(), order.getOrderCode());
            return mapper.toDto(saved);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating batch record: {}", e.getMessage(), e);
            throw new DataExistException("Hồ sơ lô sản xuất cho bước này đã tồn tại");
        } catch (Exception e) {
            log.error("Error creating batch record for order {}: {}", req.getProductionOrderId(), e.getMessage(), e);
            throw new MyCustomException(CREATE_ERROR);
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionBatchRecord", description = "Cập nhật hồ sơ lô sản xuất")
    public ProductionBatchRecordDto update(UpdateProductionBatchRecordRequest req) {
        validateUpdateRequest(req);

        ProductionBatchRecord entity = findRecordById(req.getId());

        // Check if record can be updated (business rule)
        validateUpdatePermissions(entity);

        // Store original values for logging
        String originalStepName = entity.getStepName();

        mapper.updateFromRequest(req, entity);

        try {
            ProductionBatchRecord updated = recordRepo.saveAndFlush(entity);
            log.info("Updated production batch record ID: {} (step: {} -> {})",
                    updated.getId(), originalStepName, updated.getStepName());
            return mapper.toDto(updated);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating batch record {}: {}", req.getId(), e.getMessage(), e);
            throw new DataExistException("Vi phạm ràng buộc dữ liệu khi cập nhật");
        } catch (Exception e) {
            log.error("Error updating batch record {}: {}", req.getId(), e.getMessage(), e);
            throw new MyCustomException(UPDATE_ERROR);
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "ProductionBatchRecord", description = "Xóa hồ sơ lô sản xuất")
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID không được null");
        }

        ProductionBatchRecord record = findRecordById(id);

        // Validate delete permissions
        validateDeletePermissions(record);

        try {
            recordRepo.deleteById(id);
            log.info("Deleted production batch record ID: {} (step: {})", id, record.getStepName());
        } catch (Exception e) {
            log.error("Error deleting batch record {}: {}", id, e.getMessage(), e);
            throw new MyCustomException(DELETE_ERROR);
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "ProductionBatchRecord", description = "Xóa danh sách hồ sơ lô sản xuất")
    public List<ProductionBatchRecordDto> deleteAll(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new IllegalArgumentException("Danh sách ID không được rỗng");
        }

        // Remove duplicates and validate
        Set<Long> uniqueIds = ids.stream().collect(Collectors.toSet());

        // Fetch all records in one query for better performance
        List<ProductionBatchRecord> records = recordRepo.findAllById(uniqueIds);

        if (records.size() != uniqueIds.size()) {
            Set<Long> foundIds = records.stream().map(ProductionBatchRecord::getId).collect(Collectors.toSet());
            Set<Long> missingIds = uniqueIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());
            throw new DataNotFoundException("Không tìm thấy hồ sơ với ID: " + missingIds);
        }

        // Validate all records can be deleted
        for (ProductionBatchRecord record : records) {
            validateDeletePermissions(record);
        }

        // Convert to DTOs before deletion
        List<ProductionBatchRecordDto> result = records.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        try {
            // Batch delete for better performance
            recordRepo.deleteAllInBatch(records);
            log.info("Batch deleted {} production batch records", records.size());
            return result;
        } catch (Exception e) {
            log.error("Error batch deleting {} records: {}", records.size(), e.getMessage(), e);
            throw new MyCustomException("Có lỗi xảy ra khi xóa danh sách hồ sơ lô sản xuất");
        }
    }

    // Private helper methods
    private ProductionBatchRecord findRecordById(Long id) {
        return recordRepo.findById(id)
                .orElseThrow(() -> new DataNotFoundException(RECORD_NOT_FOUND));
    }

    private ProductionOrder validateAndGetProductionOrder(Long orderId) {
        ProductionOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException(ORDER_NOT_FOUND));

        // Check if production order is in a valid status for creating records
        if (order.getStatus() == ProductionOrderStatus.CANCELLED) {
            throw new MyCustomException("Không thể tạo hồ sơ cho lệnh sản xuất đã hủy");
        }

        return order;
    }

    private void validateCreateRequest(CreateProductionBatchRecordRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Request không được null");
        }
        if (req.getProductionOrderId() == null) {
            throw new IllegalArgumentException("Production Order ID không được null");
        }
        if (req.getStepName() == null || req.getStepName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên bước không được để trống");
        }
        if (req.getRecordDate() == null) {
            throw new IllegalArgumentException("Ngày ghi nhận không được null");
        }
    }

    private void validateUpdateRequest(UpdateProductionBatchRecordRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Request không được null");
        }
        if (req.getId() == null) {
            throw new IllegalArgumentException("ID không được null");
        }
    }

    private void validateBusinessRules(CreateProductionBatchRecordRequest req, ProductionOrder order) {
        // Example: Check if step already exists for this order
        if (req.getStepName() != null && recordRepo.existsByProductionOrderIdAndStepName(
                req.getProductionOrderId(), req.getStepName())) {
            throw new DataExistException("Bước '" + req.getStepName() + "' đã tồn tại cho lệnh sản xuất này");
        }

        // Example: Validate record date is within production order timeline
        if (req.getRecordDate().isBefore(order.getPlannedStartDate())) {
            throw new MyCustomException("Ngày ghi nhận không được trước ngày bắt đầu kế hoạch");
        }
    }

    private void validateUpdatePermissions(ProductionBatchRecord record) {
        // Example: Don't allow updating approved records
        if (record.isApproved()) {
            throw new MyCustomException("Không thể cập nhật hồ sơ đã được duyệt");
        }

        // Example: Check if production order is still active
        if (record.getProductionOrder().getStatus() == ProductionOrderStatus.CANCELLED) {
            throw new MyCustomException("Không thể cập nhật hồ sơ của lệnh sản xuất đã hủy");
        }
    }

    private void validateDeletePermissions(ProductionBatchRecord record) {
        // Example: Don't allow deleting approved records
        if (record.isApproved()) {
            throw new MyCustomException("Không thể xóa hồ sơ đã được duyệt");
        }

        // Example: Check if there are dependent records
        // if (hasDependent Records(record)) {
        //     throw new MyCustomException("Không thể xóa hồ sơ có dữ liệu phụ thuộc");
        // }
    }

    private Integer getNextSequenceNumber(Long productionOrderId, String stepName) {
        // Get the maximum sequence number for this order and step
        Integer maxSequence = recordRepo.findMaxSequenceNumberByProductionOrderIdAndStepName(
                productionOrderId, stepName);
        return maxSequence != null ? maxSequence + 1 : 1;
    }
}