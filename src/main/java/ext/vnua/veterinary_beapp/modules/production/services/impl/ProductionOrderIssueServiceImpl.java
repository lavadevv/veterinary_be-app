package ext.vnua.veterinary_beapp.modules.production.services.impl;

import ext.vnua.veterinary_beapp.exception.BusinessException;
import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.DataNotFoundException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderIssueDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionOrderIssue.CreateProductionOrderIssueRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionOrderIssue.UpdateProductionOrderIssueRequest;
import ext.vnua.veterinary_beapp.modules.production.enums.IssueStatus;
import ext.vnua.veterinary_beapp.modules.production.enums.IssueType;
import ext.vnua.veterinary_beapp.modules.production.enums.ProductionOrderStatus;
import ext.vnua.veterinary_beapp.modules.production.mapper.ProductionOrderIssueMapper;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrderIssue;
import ext.vnua.veterinary_beapp.modules.production.repository.ProductionOrderIssueRepository;
import ext.vnua.veterinary_beapp.modules.production.repository.ProductionOrderRepository;
import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionOrderIssueQuery;
import ext.vnua.veterinary_beapp.modules.production.services.ProductionOrderIssueService;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class ProductionOrderIssueServiceImpl implements ProductionOrderIssueService {

    private final ProductionOrderIssueRepository issueRepo;
    private final ProductionOrderRepository orderRepo;
    private final UserRepository userRepo;
    private final ProductionOrderIssueMapper mapper;

    // Cache for issue code generation to prevent concurrent duplicates
    private final Map<String, Object> issueCodeLocks = new ConcurrentHashMap<>();

    // Constants
    private static final String ISSUE_NOT_FOUND = "Phiếu cấp phát không tồn tại";
    private static final String ORDER_NOT_FOUND = "Lệnh sản xuất không tồn tại";
    private static final String USER_NOT_FOUND = "Người dùng không tồn tại";
    private static final String INVALID_ORDER_STATUS = "Lệnh sản xuất không ở trạng thái hợp lệ để tạo phiếu cấp phát";
    private static final String INVALID_ISSUE_STATUS = "Trạng thái phiếu cấp phát không hợp lệ cho thao tác này";
    private static final String INVALID_DATE_RANGE = "Ngày cấp phát phải nằm trong khoảng thời gian của lệnh sản xuất";
    private static final String DUPLICATE_ISSUE_TYPE = "Đã tồn tại phiếu cấp phát loại {} cho lệnh sản xuất này";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public Page<ProductionOrderIssueDto> getAll(CustomProductionOrderIssueQuery.ProductionOrderIssueFilterParam param, PageRequest pageRequest) {
        try {
            validateFilterParameters(param);
            Specification<ProductionOrderIssue> spec = CustomProductionOrderIssueQuery.getFilter(param);
            return issueRepo.findAll(spec, pageRequest).map(mapper::toDto);
        } catch (Exception e) {
            log.error("Error fetching ProductionOrderIssues with filter {}", param, e);
            throw new MyCustomException("Có lỗi xảy ra khi tải danh sách phiếu cấp phát: " + e.getMessage());
        }
    }

    @Override
    public ProductionOrderIssueDto getById(Long id) {
        validateId(id, "ID phiếu cấp phát");

        ProductionOrderIssue issue = issueRepo.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ISSUE_NOT_FOUND + " với ID: " + id));
        return mapper.toDto(issue);
    }

    @Override
    public ProductionOrderIssueDto getByCode(String issueCode) {
        validateString(issueCode, "Mã phiếu cấp phát");

        ProductionOrderIssue issue = issueRepo.findByIssueCode(issueCode)
                .orElseThrow(() -> new DataNotFoundException(ISSUE_NOT_FOUND + " với mã: " + issueCode));
        return mapper.toDto(issue);
    }

    @Override
    public List<ProductionOrderIssueDto> getByOrder(Long orderId) {
        validateId(orderId, "ID lệnh sản xuất");

        // Verify order exists
        if (!orderRepo.existsById(orderId)) {
            throw new DataNotFoundException(ORDER_NOT_FOUND + " với ID: " + orderId);
        }

        List<ProductionOrderIssue> issues = issueRepo.findByProductionOrderId(orderId);
        return issues.stream()
                .map(mapper::toDto)
                .sorted(Comparator.comparing(ProductionOrderIssueDto::getId).reversed())
                .toList();
    }

    @Override
    public List<ProductionOrderIssueDto> getByOrderAndType(Long orderId, IssueType issueType) {
        validateId(orderId, "ID lệnh sản xuất");
        Objects.requireNonNull(issueType, "Loại phiếu cấp phát không được null");

        List<ProductionOrderIssue> issues = issueRepo.findByProductionOrderIdAndIssueType(orderId, issueType);
        return issues.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<ProductionOrderIssueDto> getPendingIssues() {
        List<ProductionOrderIssue> pendingIssues = issueRepo.findByStatus(IssueStatus.PENDING);
        return pendingIssues.stream()
                .map(mapper::toDto)
                .sorted(Comparator.comparing(ProductionOrderIssueDto::getId).reversed())
                .toList();
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductionOrderIssue", description = "Tạo mới phiếu cấp phát")
    public ProductionOrderIssueDto create(@Valid CreateProductionOrderIssueRequest request) {
        log.debug("Creating new ProductionOrderIssue with request: {}", request);

        // Validate and get production order
        ProductionOrder order = validateAndGetProductionOrder(request.getProductionOrderId());

        // Business validations
        validateCreateBusinessRules(request, order);

        // Create entity
        ProductionOrderIssue entity = mapper.toCreate(request);
        entity.setProductionOrder(order);

        // Generate unique issue code with thread safety
        String issueCode = generateUniqueIssueCode(order);
        entity.setIssueCode(issueCode);

        try {
            ProductionOrderIssue saved = issueRepo.saveAndFlush(entity);
            log.info("Successfully created ProductionOrderIssue {} for order {}", issueCode, order.getOrderCode());
            return mapper.toDto(saved);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation creating issue: {}", e.getMessage(), e);
            throw new DataExistException("Phiếu cấp phát với mã " + issueCode + " đã tồn tại");
        } catch (Exception e) {
            log.error("Unexpected error creating issue: {}", e.getMessage(), e);
            throw new MyCustomException("Có lỗi xảy ra khi tạo phiếu cấp phát: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrderIssue", description = "Cập nhật phiếu cấp phát")
    public ProductionOrderIssueDto update(@Valid UpdateProductionOrderIssueRequest request) {
        log.debug("Updating ProductionOrderIssue with request: {}", request);

        ProductionOrderIssue entity = issueRepo.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException(ISSUE_NOT_FOUND + " với ID: " + request.getId()));

        // Business validations
        validateUpdateBusinessRules(request, entity);

        // Store old status for logging
        IssueStatus oldStatus = entity.getStatus();

        mapper.updateFromRequest(request, entity);

        try {
            ProductionOrderIssue updated = issueRepo.saveAndFlush(entity);

            if (oldStatus != updated.getStatus()) {
                log.info("ProductionOrderIssue {} status changed from {} to {}",
                        updated.getIssueCode(), oldStatus, updated.getStatus());
            } else {
                log.info("Updated ProductionOrderIssue {}", updated.getIssueCode());
            }

            return mapper.toDto(updated);
        } catch (Exception e) {
            log.error("Error updating ProductionOrderIssue {}: {}", request.getId(), e.getMessage(), e);
            throw new MyCustomException("Có lỗi xảy ra khi cập nhật phiếu cấp phát: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrderIssue", description = "Phê duyệt phiếu cấp phát")
    public ProductionOrderIssueDto approve(Long issueId, Long approverId) {
        validateId(issueId, "ID phiếu cấp phát");
        validateId(approverId, "ID người duyệt");

        ProductionOrderIssue issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new DataNotFoundException(ISSUE_NOT_FOUND + " với ID: " + issueId));

        User approver = userRepo.findById(approverId)
                .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND + " với ID: " + approverId));

        // Validate business rules
        if (!issue.isPending()) {
            throw new BusinessException("Chỉ có thể phê duyệt phiếu cấp phát ở trạng thái chờ xử lý");
        }

        issue.setStatus(IssueStatus.COMPLETED);
        issue.setApprovedBy(approver);

        try {
            ProductionOrderIssue saved = issueRepo.saveAndFlush(issue);
            log.info("Approved ProductionOrderIssue {} by user {}", issue.getIssueCode(), approver.getEmail());
            return mapper.toDto(saved);
        } catch (Exception e) {
            log.error("Error approving issue {}: {}", issueId, e.getMessage(), e);
            throw new MyCustomException("Có lỗi xảy ra khi phê duyệt phiếu cấp phát: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionOrderIssue", description = "Hủy phiếu cấp phát")
    public ProductionOrderIssueDto cancel(Long issueId, String reason) {
        validateId(issueId, "ID phiếu cấp phát");

        ProductionOrderIssue issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new DataNotFoundException(ISSUE_NOT_FOUND + " với ID: " + issueId));

        // Validate business rules
        if (issue.isCompleted()) {
            throw new BusinessException("Không thể hủy phiếu cấp phát đã hoàn tất");
        }

        if (issue.isCancelled()) {
            throw new BusinessException("Phiếu cấp phát đã được hủy trước đó");
        }

        issue.setStatus(IssueStatus.CANCELLED);
        if (StringUtils.hasText(reason)) {
            String currentNotes = issue.getNotes();
            String cancelNote = "Lý do hủy: " + reason;
            issue.setNotes(currentNotes == null ? cancelNote : currentNotes + "\n" + cancelNote);
        }

        try {
            ProductionOrderIssue saved = issueRepo.saveAndFlush(issue);
            log.info("Cancelled ProductionOrderIssue {} with reason: {}", issue.getIssueCode(), reason);
            return mapper.toDto(saved);
        } catch (Exception e) {
            log.error("Error cancelling issue {}: {}", issueId, e.getMessage(), e);
            throw new MyCustomException("Có lỗi xảy ra khi hủy phiếu cấp phát: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "ProductionOrderIssue", description = "Xóa phiếu cấp phát")
    public void delete(Long id) {
        validateId(id, "ID phiếu cấp phát");

        ProductionOrderIssue issue = issueRepo.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ISSUE_NOT_FOUND + " với ID: " + id));

        // Business validation - only allow deletion of pending issues
        if (!issue.isPending()) {
            throw new BusinessException("Chỉ có thể xóa phiếu cấp phát ở trạng thái chờ xử lý");
        }

        try {
            issueRepo.delete(issue);
            log.info("Deleted ProductionOrderIssue {} (ID: {})", issue.getIssueCode(), id);
        } catch (Exception e) {
            log.error("Error deleting ProductionOrderIssue {}: {}", id, e.getMessage(), e);
            throw new MyCustomException("Có lỗi xảy ra khi xóa phiếu cấp phát: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "ProductionOrderIssue", description = "Xóa danh sách phiếu cấp phát")
    public List<ProductionOrderIssueDto> deleteAll(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new IllegalArgumentException("Danh sách ID không được rỗng");
        }

        // Remove duplicates
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);

        List<ProductionOrderIssue> issues = issueRepo.findAllById(uniqueIds);

        // Check if all IDs exist
        if (issues.size() != uniqueIds.size()) {
            Set<Long> foundIds = issues.stream().map(ProductionOrderIssue::getId).collect(Collectors.toSet());
            Set<Long> missingIds = uniqueIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());
            throw new DataNotFoundException("Không tìm thấy phiếu cấp phát với ID: " + missingIds);
        }

        // Validate business rules - only allow deletion of pending issues
        List<ProductionOrderIssue> nonPendingIssues = issues.stream()
                .filter(issue -> !issue.isPending())
                .toList();

        if (!nonPendingIssues.isEmpty()) {
            List<String> codes = nonPendingIssues.stream()
                    .map(ProductionOrderIssue::getIssueCode)
                    .toList();
            throw new BusinessException("Không thể xóa các phiếu cấp phát không ở trạng thái chờ xử lý: " + codes);
        }

        List<ProductionOrderIssueDto> result = issues.stream().map(mapper::toDto).toList();

        try {
            issueRepo.deleteAllInBatch(issues);
            log.info("Batch deleted {} ProductionOrderIssues: {}",
                    issues.size(),
                    issues.stream().map(ProductionOrderIssue::getIssueCode).collect(Collectors.toList()));
            return result;
        } catch (Exception e) {
            log.error("Error batch deleting issues: {}", e.getMessage(), e);
            throw new MyCustomException("Có lỗi xảy ra khi xóa danh sách phiếu cấp phát: " + e.getMessage());
        }
    }

    @Override
    public Map<IssueStatus, Long> getStatusStatistics(Long orderId) {
        if (orderId != null) {
            validateId(orderId, "ID lệnh sản xuất");
            return issueRepo.countByProductionOrderIdGroupByStatus(orderId);
        }
        return issueRepo.countAllGroupByStatus();
    }

    @Override
    public boolean hasActiveIssues(Long orderId) {
        validateId(orderId, "ID lệnh sản xuất");
        return issueRepo.existsByProductionOrderIdAndStatus(orderId, IssueStatus.PENDING);
    }

    // ========== Helper Methods ==========

    private void validateFilterParameters(CustomProductionOrderIssueQuery.ProductionOrderIssueFilterParam param) {
        if (param == null) return;

        if (param.getFromIssueDate() != null && param.getToIssueDate() != null) {
            if (param.getFromIssueDate().isAfter(param.getToIssueDate())) {
                throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
            }
        }
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ");
        }
    }

    private void validateString(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }
    }

    private ProductionOrder validateAndGetProductionOrder(Long orderId) {
        validateId(orderId, "ID lệnh sản xuất");

        return orderRepo.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException(ORDER_NOT_FOUND + " với ID: " + orderId));
    }

    private void validateCreateBusinessRules(CreateProductionOrderIssueRequest request, ProductionOrder order) {
        // Validate production order status
        if (order.getStatus() == ProductionOrderStatus.CANCELLED ||
                order.getStatus() == ProductionOrderStatus.COMPLETED) {
            throw new BusinessException(INVALID_ORDER_STATUS + ": " + order.getStatus().name());
        }

        // Validate issue date within production order date range
        validateIssueDateRange(request.getIssueDate(), order);

        // Check for duplicate issue type (business rule: one issue per type per order)
        if (issueRepo.existsByProductionOrderIdAndIssueType(order.getId(), request.getIssueType())) {
            throw new BusinessException(String.format(DUPLICATE_ISSUE_TYPE, request.getIssueType().getDescription()));
        }

        // Validate issue date is not in the future
        if (request.getIssueDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Ngày cấp phát không thể ở tương lai");
        }
    }

    private void validateUpdateBusinessRules(UpdateProductionOrderIssueRequest request, ProductionOrderIssue entity) {
        // Cannot update cancelled issues
        if (entity.isCancelled()) {
            throw new BusinessException("Không thể cập nhật phiếu cấp phát đã bị hủy");
        }

        // Validate status transitions
        if (request.getStatus() != null) {
            validateStatusTransition(entity.getStatus(), request.getStatus());
        }

        // Validate issue date if changed
        if (request.getIssueDate() != null && !request.getIssueDate().equals(entity.getIssueDate())) {
            validateIssueDateRange(request.getIssueDate(), entity.getProductionOrder());

            // Cannot change date of completed issues
            if (entity.isCompleted()) {
                throw new BusinessException("Không thể thay đổi ngày cấp phát của phiếu đã hoàn tất");
            }
        }
    }

    private void validateStatusTransition(IssueStatus currentStatus, IssueStatus newStatus) {
        // Define valid transitions
        Map<IssueStatus, Set<IssueStatus>> validTransitions = Map.of(
                IssueStatus.PENDING, Set.of(IssueStatus.COMPLETED, IssueStatus.CANCELLED),
                IssueStatus.COMPLETED, Set.of(IssueStatus.CANCELLED), // Allow cancelling completed issues if needed
                IssueStatus.CANCELLED, Set.of() // No transitions from cancelled
        );

        if (!validTransitions.get(currentStatus).contains(newStatus)) {
            throw new BusinessException(String.format(
                    "Không thể chuyển từ trạng thái %s sang %s",
                    currentStatus.getDescription(),
                    newStatus.getDescription()
            ));
        }
    }

    private void validateIssueDateRange(LocalDate issueDate, ProductionOrder order) {
        if (order.getPlannedStartDate() != null && issueDate.isBefore(order.getPlannedStartDate())) {
            throw new BusinessException(INVALID_DATE_RANGE + " (trước ngày bắt đầu kế hoạch)");
        }

        if (order.getPlannedEndDate() != null && issueDate.isAfter(order.getPlannedEndDate())) {
            throw new BusinessException(INVALID_DATE_RANGE + " (sau ngày kết thúc kế hoạch)");
        }
    }

    private String generateUniqueIssueCode(ProductionOrder order) {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String lockKey = order.getId() + "-" + dateStr;

        // Use synchronized block with specific lock for this order and date
        synchronized (issueCodeLocks.computeIfAbsent(lockKey, k -> new Object())) {
            try {
                // Count issues for this order and date to generate sequence
                long sequenceNumber = issueRepo.countByProductionOrderIdAndIssueDateBetween(
                        order.getId(),
                        LocalDate.now(),
                        LocalDate.now()
                ) + 1;

                String issueCode;
                int maxAttempts = 10;
                int attempts = 0;

                do {
                    issueCode = String.format("ISS-%s-%03d", dateStr, sequenceNumber + attempts);
                    attempts++;
                } while (issueRepo.existsByIssueCode(issueCode) && attempts < maxAttempts);

                if (attempts >= maxAttempts) {
                    throw new MyCustomException("Không thể tạo mã phiếu cấp phát duy nhất sau " + maxAttempts + " lần thử");
                }

                return issueCode;
            } finally {
                // Clean up lock if no longer needed
                issueCodeLocks.remove(lockKey);
            }
        }
    }
}