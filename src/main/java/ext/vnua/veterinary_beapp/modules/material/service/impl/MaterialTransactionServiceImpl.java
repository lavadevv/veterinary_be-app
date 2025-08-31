package ext.vnua.veterinary_beapp.modules.material.service.impl;


import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialTransactionDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialTransaction.CreateMaterialTransactionRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialTransaction.UpdateMaterialTransactionRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.MaterialTransactionMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialTransaction;
import ext.vnua.veterinary_beapp.modules.material.repository.LocationRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialTransactionRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialTransactionQuery;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialTransactionService;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaterialTransactionServiceImpl implements MaterialTransactionService {
    private final MaterialTransactionRepository materialTransactionRepository;
    private final MaterialBatchRepository materialBatchRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final MaterialTransactionMapper materialTransactionMapper;

    @Override
    public Page<MaterialTransaction> getAllMaterialTransaction(CustomMaterialTransactionQuery.MaterialTransactionFilterParam param, PageRequest pageRequest) {
        Specification<MaterialTransaction> specification = CustomMaterialTransactionQuery.getFilterMaterialTransaction(param);
        return materialTransactionRepository.findAll(specification, pageRequest);
    }

    @Override
    public MaterialTransactionDto selectMaterialTransactionById(Long id) {
        Optional<MaterialTransaction> transactionOptional = materialTransactionRepository.findById(id);
        if (transactionOptional.isEmpty()) {
            throw new DataExistException("Giao dịch vật liệu không tồn tại");
        }
        MaterialTransaction transaction = transactionOptional.get();
        return materialTransactionMapper.toMaterialTransactionDto(transaction);
    }

    @Override
    public List<MaterialTransactionDto> selectMaterialTransactionsByBatch(Long materialBatchId) {
        List<MaterialTransaction> transactions = materialTransactionRepository.findByMaterialBatchId(materialBatchId);
        return transactions.stream()
                .map(materialTransactionMapper::toMaterialTransactionDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MaterialTransactionDto> selectMaterialTransactionsByType(MaterialTransaction.TransactionType transactionType) {
        List<MaterialTransaction> transactions = materialTransactionRepository.findByTransactionType(transactionType);
        return transactions.stream()
                .map(materialTransactionMapper::toMaterialTransactionDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MaterialTransactionDto> selectMaterialTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<MaterialTransaction> transactions = materialTransactionRepository.findByTransactionDateBetween(startDate, endDate);
        return transactions.stream()
                .map(materialTransactionMapper::toMaterialTransactionDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "MaterialTransaction", description = "Tạo mới giao dịch vật liệu")
    public MaterialTransactionDto createMaterialTransaction(CreateMaterialTransactionRequest request) {
        // Validate material batch exists
        MaterialBatch materialBatch = materialBatchRepository.findById(request.getMaterialBatchId())
                .orElseThrow(() -> new DataExistException("Batch vật liệu không tồn tại"));

        // Validate locations if provided
        Location fromLocation = null;
        Location toLocation = null;

        if (request.getFromLocationId() != null) {
            fromLocation = locationRepository.findById(request.getFromLocationId())
                    .orElseThrow(() -> new DataExistException("Vị trí xuất không tồn tại"));
        }

        if (request.getToLocationId() != null) {
            toLocation = locationRepository.findById(request.getToLocationId())
                    .orElseThrow(() -> new DataExistException("Vị trí nhập không tồn tại"));
        }

        // Validate approved by user if provided
        User approvedBy = null;
        if (request.getApprovedById() != null) {
            approvedBy = userRepository.findById(request.getApprovedById())
                    .orElseThrow(() -> new DataExistException("Người phê duyệt không tồn tại"));
        }

        // Business validation
        validateTransactionBusiness(request, materialBatch);

        try {
            MaterialTransaction transaction = materialTransactionMapper.toCreateMaterialTransaction(request);
            transaction.setMaterialBatch(materialBatch);
            transaction.setFromLocation(fromLocation);
            transaction.setToLocation(toLocation);
            transaction.setApprovedBy(approvedBy);

            // Calculate total value if not provided
            if (transaction.getTotalValue() == null &&
                    transaction.getUnitPrice() != null &&
                    transaction.getQuantity() != null) {
                transaction.setTotalValue(transaction.getUnitPrice().multiply(transaction.getQuantity()));
            }

            return materialTransactionMapper.toMaterialTransactionDto(
                    materialTransactionRepository.saveAndFlush(transaction));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm giao dịch vật liệu");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialTransaction", description = "Cập nhật giao dịch vật liệu")
    public MaterialTransactionDto updateMaterialTransaction(UpdateMaterialTransactionRequest request) {
        Optional<MaterialTransaction> transactionOptional = materialTransactionRepository.findById(request.getId());
        if (transactionOptional.isEmpty()) {
            throw new DataExistException("Giao dịch vật liệu không tồn tại");
        }

        MaterialTransaction existingTransaction = transactionOptional.get();

        // Validate material batch exists
        MaterialBatch materialBatch = materialBatchRepository.findById(request.getMaterialBatchId())
                .orElseThrow(() -> new DataExistException("Batch vật liệu không tồn tại"));

        // Validate locations if provided
        Location fromLocation = null;
        Location toLocation = null;

        if (request.getFromLocationId() != null) {
            fromLocation = locationRepository.findById(request.getFromLocationId())
                    .orElseThrow(() -> new DataExistException("Vị trí xuất không tồn tại"));
        }

        if (request.getToLocationId() != null) {
            toLocation = locationRepository.findById(request.getToLocationId())
                    .orElseThrow(() -> new DataExistException("Vị trí nhập không tồn tại"));
        }

        // Validate approved by user if provided
        User approvedBy = null;
        if (request.getApprovedById() != null) {
            approvedBy = userRepository.findById(request.getApprovedById())
                    .orElseThrow(() -> new DataExistException("Người phê duyệt không tồn tại"));
        }

        // Business validation
        validateTransactionBusiness(request, materialBatch);

        try {
            materialTransactionMapper.updateMaterialTransactionFromRequest(request, existingTransaction);
            existingTransaction.setMaterialBatch(materialBatch);
            existingTransaction.setFromLocation(fromLocation);
            existingTransaction.setToLocation(toLocation);
            existingTransaction.setApprovedBy(approvedBy);

            // Calculate total value if not provided
            if (existingTransaction.getTotalValue() == null &&
                    existingTransaction.getUnitPrice() != null &&
                    existingTransaction.getQuantity() != null) {
                existingTransaction.setTotalValue(existingTransaction.getUnitPrice().multiply(existingTransaction.getQuantity()));
            }

            return materialTransactionMapper.toMaterialTransactionDto(
                    materialTransactionRepository.saveAndFlush(existingTransaction));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật giao dịch vật liệu");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "MaterialTransaction", description = "Xóa giao dịch vật liệu")
    public void deleteMaterialTransaction(Long id) {
        Optional<MaterialTransaction> transactionOptional = materialTransactionRepository.findById(id);
        if (transactionOptional.isEmpty()) {
            throw new DataExistException("Giao dịch vật liệu không tồn tại");
        }

        try {
            materialTransactionRepository.deleteById(id);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa giao dịch vật liệu");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "MaterialTransaction", description = "Xóa danh sách giao dịch vật liệu")
    public List<MaterialTransactionDto> deleteAllIdMaterialTransactions(List<Long> ids) {
        List<MaterialTransactionDto> transactionDtos = new ArrayList<>();
        for (Long id : ids) {
            Optional<MaterialTransaction> optionalTransaction = materialTransactionRepository.findById(id);
            if (optionalTransaction.isPresent()) {
                MaterialTransaction transaction = optionalTransaction.get();
                transactionDtos.add(materialTransactionMapper.toMaterialTransactionDto(transaction));
                materialTransactionRepository.delete(transaction);
            } else {
                throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách giao dịch vật liệu!");
            }
        }
        return transactionDtos;
    }

    @Override
    public Double getTotalQuantityByBatchAndType(Long materialBatchId, MaterialTransaction.TransactionType transactionType) {
        Double total = materialTransactionRepository.sumQuantityByMaterialBatchAndType(materialBatchId, transactionType);
        return total != null ? total : 0.0;
    }

    @Override
    public List<MaterialTransactionDto> getWarehouseTransactionsByDateRange(Long warehouseId, LocalDateTime startDate, LocalDateTime endDate) {
        List<MaterialTransaction> transactions = materialTransactionRepository.findWarehouseTransactionsByDateRange(warehouseId, startDate, endDate);
        return transactions.stream()
                .map(materialTransactionMapper::toMaterialTransactionDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void approveMaterialTransaction(Long transactionId, Long approvedById) {
        Optional<MaterialTransaction> transactionOptional = materialTransactionRepository.findById(transactionId);
        if (transactionOptional.isEmpty()) {
            throw new DataExistException("Giao dịch vật liệu không tồn tại");
        }

        User approvedBy = userRepository.findById(approvedById)
                .orElseThrow(() -> new DataExistException("Người phê duyệt không tồn tại"));

        MaterialTransaction transaction = transactionOptional.get();
        transaction.setApprovedBy(approvedBy);
        materialTransactionRepository.saveAndFlush(transaction);
    }

    private void validateTransactionBusiness(CreateMaterialTransactionRequest request, MaterialBatch materialBatch) {
        // Validate transaction date
        if (request.getTransactionDate().isAfter(LocalDateTime.now())) {
            throw new MyCustomException("Thời gian giao dịch không được trong tương lai");
        }

        // Validate reference document uniqueness if provided
        if (request.getReferenceDocument() != null && !request.getReferenceDocument().trim().isEmpty()) {
            Optional<MaterialTransaction> existingTransaction =
                    materialTransactionRepository.findByReferenceDocument(request.getReferenceDocument());
            if (existingTransaction.isPresent()) {
                throw new DataExistException("Số chứng từ đã tồn tại");
            }
        }

        // Validate locations based on transaction type
        validateLocationsForTransactionType(request);

        // Validate quantity constraints based on transaction type
        validateQuantityConstraints(request, materialBatch);
    }

    private void validateTransactionBusiness(UpdateMaterialTransactionRequest request, MaterialBatch materialBatch) {
        // Validate transaction date
        if (request.getTransactionDate().isAfter(LocalDateTime.now())) {
            throw new MyCustomException("Thời gian giao dịch không được trong tương lai");
        }

        // Validate locations based on transaction type
        validateLocationsForTransactionType(request);

        // Validate quantity constraints based on transaction type
        validateQuantityConstraints(request, materialBatch);
    }

    private void validateLocationsForTransactionType(CreateMaterialTransactionRequest request) {
        switch (request.getTransactionType()) {
            case NHAP_KHO:
                if (request.getToLocationId() == null) {
                    throw new MyCustomException("Vị trí nhập là bắt buộc cho giao dịch nhập kho");
                }
                break;
            case XUAT_KHO:
                if (request.getFromLocationId() == null) {
                    throw new MyCustomException("Vị trí xuất là bắt buộc cho giao dịch xuất kho");
                }
                break;
            case DIEU_CHINH:
                // Điều chỉnh có thể không cần location hoặc chỉ cần một location
                break;
            case TRA_HANG:
                if (request.getFromLocationId() == null) {
                    throw new MyCustomException("Vị trí xuất là bắt buộc cho giao dịch trả hàng");
                }
                break;
            case HUY_BO:
                if (request.getFromLocationId() == null) {
                    throw new MyCustomException("Vị trí xuất là bắt buộc cho giao dịch hủy bỏ");
                }
                break;
        }
    }

    private void validateLocationsForTransactionType(UpdateMaterialTransactionRequest request) {
        switch (request.getTransactionType()) {
            case NHAP_KHO:
                if (request.getToLocationId() == null) {
                    throw new MyCustomException("Vị trí nhập là bắt buộc cho giao dịch nhập kho");
                }
                break;
            case XUAT_KHO:
                if (request.getFromLocationId() == null) {
                    throw new MyCustomException("Vị trí xuất là bắt buộc cho giao dịch xuất kho");
                }
                break;
            case DIEU_CHINH:
                // Điều chỉnh có thể không cần location hoặc chỉ cần một location
                break;
            case TRA_HANG:
                if (request.getFromLocationId() == null) {
                    throw new MyCustomException("Vị trí xuất là bắt buộc cho giao dịch trả hàng");
                }
                break;
            case HUY_BO:
                if (request.getFromLocationId() == null) {
                    throw new MyCustomException("Vị trí xuất là bắt buộc cho giao dịch hủy bỏ");
                }
                break;
        }
    }

    private void validateQuantityConstraints(CreateMaterialTransactionRequest request, MaterialBatch materialBatch) {
        // For outgoing transactions, check if there's enough quantity
        if (request.getTransactionType() == MaterialTransaction.TransactionType.XUAT_KHO ||
                request.getTransactionType() == MaterialTransaction.TransactionType.TRA_HANG ||
                request.getTransactionType() == MaterialTransaction.TransactionType.HUY_BO) {

            if (materialBatch.getCurrentQuantity().compareTo(request.getQuantity()) < 0) {
                throw new MyCustomException("Số lượng xuất vượt quá số lượng hiện có trong batch");
            }
        }
    }

    private void validateQuantityConstraints(UpdateMaterialTransactionRequest request, MaterialBatch materialBatch) {
        // For outgoing transactions, check if there's enough quantity
        if (request.getTransactionType() == MaterialTransaction.TransactionType.XUAT_KHO ||
                request.getTransactionType() == MaterialTransaction.TransactionType.TRA_HANG ||
                request.getTransactionType() == MaterialTransaction.TransactionType.HUY_BO) {

            if (materialBatch.getCurrentQuantity().compareTo(request.getQuantity()) < 0) {
                throw new MyCustomException("Số lượng xuất vượt quá số lượng hiện có trong batch");
            }
        }
    }
}