package ext.vnua.veterinary_beapp.modules.production.services.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.DataNotFoundException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.production.constants.ProductionLineConstants;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.production.dto.ProductionLineDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionLine.CreateProductionLineRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionLine.UpdateProductionLineRequest;
import ext.vnua.veterinary_beapp.modules.production.mapper.ProductionLineMapper;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionLine;
import ext.vnua.veterinary_beapp.modules.production.repository.ProductionLineRepository;
import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionLineQuery;
import ext.vnua.veterinary_beapp.modules.production.services.ProductionLineService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionLineServiceImpl implements ProductionLineService {

    private final ProductionLineRepository lineRepo;
    private final ProductionLineMapper lineMapper;

    @Override
    public Page<ProductionLine> getAllLines(CustomProductionLineQuery.ProductionLineFilterParam param, PageRequest pr) {
        Specification<ProductionLine> spec = CustomProductionLineQuery.getFilter(param);
        return lineRepo.findAll(spec, pr);
    }

    @Override
    public ProductionLineDto getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID không được null");
        }

        ProductionLine line = lineRepo.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ProductionLineConstants.ValidationMessages.LINE_NOT_FOUND + " với ID: " + id));
        return lineMapper.toDto(line);
    }

    @Override
    public ProductionLineDto getByCode(String lineCode) {
        if (lineCode == null || lineCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dây chuyền không được null hoặc rỗng");
        }

        ProductionLine line = lineRepo.findByLineCode(lineCode.trim())
                .orElseThrow(() -> new DataNotFoundException(ProductionLineConstants.ValidationMessages.LINE_NOT_FOUND + ": " + lineCode));
        return lineMapper.toDto(line);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductionLine", description = "Tạo mới dây chuyền sản xuất")
    public ProductionLineDto create(CreateProductionLineRequest req) {
        validateCreateRequest(req);

        // Check duplicate
        if (lineRepo.findByLineCode(req.getLineCode().trim()).isPresent()) {
            throw new DataExistException(ProductionLineConstants.ValidationMessages.LINE_CODE_EXISTS + ": " + req.getLineCode());
        }

        try {
            ProductionLine line = lineMapper.toCreate(req);
            line.setLineCode(req.getLineCode().trim()); // Ensure trimmed
            line.setStatus(ProductionLineConstants.Status.ACTIVE); // Default status

            ProductionLine saved = lineRepo.saveAndFlush(line);
            log.info("Created production line with ID: {} and code: {}", saved.getId(), saved.getLineCode());

            return lineMapper.toDto(saved);
        } catch (Exception e) {
            log.error("Error creating production line: {}", e.getMessage(), e);
            throw new MyCustomException("Lỗi khi tạo dây chuyền sản xuất");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionLine", description = "Cập nhật dây chuyền sản xuất")
    public ProductionLineDto update(UpdateProductionLineRequest req) {
        validateUpdateRequest(req);

        ProductionLine existingLine = lineRepo.findById(req.getId())
                .orElseThrow(() -> new DataNotFoundException(ProductionLineConstants.ValidationMessages.LINE_NOT_FOUND + " với ID: " + req.getId()));

        // Check duplicate code (only if code changed)
        String newCode = req.getLineCode().trim();
        if (!existingLine.getLineCode().equals(newCode) &&
                lineRepo.findByLineCode(newCode).isPresent()) {
            throw new DataExistException(ProductionLineConstants.ValidationMessages.LINE_CODE_EXISTS + ": " + newCode);
        }

        try {
            // Update only changed fields, preserve others
            lineMapper.updateFromRequest(req, existingLine);
            existingLine.setLineCode(newCode);

            ProductionLine saved = lineRepo.saveAndFlush(existingLine);
            log.info("Updated production line with ID: {}", saved.getId());

            return lineMapper.toDto(saved);
        } catch (Exception e) {
            log.error("Error updating production line ID {}: {}", req.getId(), e.getMessage(), e);
            throw new MyCustomException("Lỗi khi cập nhật dây chuyền sản xuất");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionLine", description = "Chuyển trạng thái dây chuyền sản xuất")
    public void toggleStatus(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID không được null");
        }

        ProductionLine line = lineRepo.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ProductionLineConstants.ValidationMessages.LINE_NOT_FOUND + " với ID: " + id));

        String newStatus = ProductionLineConstants.Status.ACTIVE.equals(line.getStatus()) ?
                ProductionLineConstants.Status.INACTIVE : ProductionLineConstants.Status.ACTIVE;
        line.setStatus(newStatus);

        lineRepo.saveAndFlush(line);
        log.info("Toggled status of production line ID {} to {}", id, newStatus);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "ProductionLine", description = "Xóa dây chuyền sản xuất")
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID không được null");
        }

        if (!lineRepo.existsById(id)) {
            throw new DataNotFoundException(ProductionLineConstants.ValidationMessages.LINE_NOT_FOUND + " với ID: " + id);
        }

        // Check if can delete (business logic)
        validateBeforeDelete(id);

        lineRepo.deleteById(id);
        log.info("Deleted production line with ID: {}", id);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "ProductionLine", description = "Xóa nhiều dây chuyền sản xuất")
    public List<ProductionLineDto> deleteAll(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new IllegalArgumentException("Danh sách ID không được null hoặc rỗng");
        }

        // Remove duplicates and nulls
        Set<Long> uniqueIds = ids.stream()
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (uniqueIds.isEmpty()) {
            throw new IllegalArgumentException("Không có ID hợp lệ trong danh sách");
        }

        // Batch fetch existing records
        List<ProductionLine> existingLines = lineRepo.findAllById(uniqueIds);

        if (existingLines.size() != uniqueIds.size()) {
            Set<Long> foundIds = existingLines.stream()
                    .map(ProductionLine::getId)
                    .collect(Collectors.toSet());
            Set<Long> missingIds = uniqueIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());
            throw new DataNotFoundException("Không tìm thấy dây chuyền với ID: " + missingIds);
        }

        // Validate business rules for each
        for (ProductionLine line : existingLines) {
            validateBeforeDelete(line.getId());
        }

        // Convert to DTOs before deletion
        List<ProductionLineDto> result = existingLines.stream()
                .map(lineMapper::toDto)
                .collect(Collectors.toList());

        // Batch delete
        lineRepo.deleteAll(existingLines);
        log.info("Batch deleted {} production lines", existingLines.size());

        return result;
    }

    // Helper methods for validation
    private void validateCreateRequest(CreateProductionLineRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Request không được null");
        }
        if (req.getLineCode() == null || req.getLineCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dây chuyền không được null hoặc rỗng");
        }
        // Add other validations as needed
    }

    private void validateUpdateRequest(UpdateProductionLineRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Request không được null");
        }
        if (req.getId() == null) {
            throw new IllegalArgumentException("ID không được null");
        }
        if (req.getLineCode() == null || req.getLineCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dây chuyền không được null hoặc rỗng");
        }
        // Add other validations as needed
    }

    private void validateBeforeDelete(Long id) {
        // TODO: Add business logic validation
        // For example: check if production line is being used in active productions
        // throw new MyCustomException("Không thể xóa dây chuyền đang được sử dụng");
    }

    // Additional utility methods
    @Override
    public boolean existsByCode(String lineCode) {
        if (lineCode == null || lineCode.trim().isEmpty()) {
            return false;
        }
        return lineRepo.existsByLineCode(lineCode.trim());
    }

    @Override
    public List<ProductionLineDto> findActiveLines() {
        List<ProductionLine> activeLines = lineRepo.findByStatusOrderByNameAsc(ProductionLineConstants.Status.ACTIVE);
        return activeLines.stream()
                .map(lineMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductionLineDto> findByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái không được null hoặc rỗng");
        }

        List<ProductionLine> lines = lineRepo.findByStatusOrderByNameAsc(status.trim());
        return lines.stream()
                .map(lineMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return 0;
        }
        return lineRepo.countByStatus(status.trim());
    }

    @Override
    public List<ProductionLineDto> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<ProductionLine> lines = lineRepo.findByNameContainingIgnoreCase(name.trim());
        return lines.stream()
                .map(lineMapper::toDto)
                .collect(Collectors.toList());
    }
}