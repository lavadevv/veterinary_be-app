package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialTransactionDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialTransaction.CreateMaterialTransactionRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialTransaction.UpdateMaterialTransactionRequest;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialTransaction;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialTransactionQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface MaterialTransactionService {
    Page<MaterialTransaction> getAllMaterialTransaction(CustomMaterialTransactionQuery.MaterialTransactionFilterParam param, PageRequest pageRequest);
    MaterialTransactionDto selectMaterialTransactionById(Long id);
    List<MaterialTransactionDto> selectMaterialTransactionsByBatch(Long materialBatchId);
    List<MaterialTransactionDto> selectMaterialTransactionsByType(MaterialTransaction.TransactionType transactionType);
    List<MaterialTransactionDto> selectMaterialTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    MaterialTransactionDto createMaterialTransaction(CreateMaterialTransactionRequest request);
    MaterialTransactionDto updateMaterialTransaction(UpdateMaterialTransactionRequest request);

    void deleteMaterialTransaction(Long id);
    List<MaterialTransactionDto> deleteAllIdMaterialTransactions(List<Long> ids);

    // Additional business methods
    Double getTotalQuantityByBatchAndType(Long materialBatchId, MaterialTransaction.TransactionType transactionType);
    List<MaterialTransactionDto> getWarehouseTransactionsByDateRange(Long warehouseId, LocalDateTime startDate, LocalDateTime endDate);
    void approveMaterialTransaction(Long transactionId, Long approvedById);
}
