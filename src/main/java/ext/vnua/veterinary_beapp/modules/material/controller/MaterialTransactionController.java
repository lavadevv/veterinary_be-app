package ext.vnua.veterinary_beapp.modules.material.controller;


import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialTransactionDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialTransaction.CreateMaterialTransactionRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialTransaction.GetMaterialTransactionRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialTransaction.UpdateMaterialTransactionRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.MaterialTransactionMapper;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialTransaction;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialTransactionService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/material-transactions")
@RequiredArgsConstructor
public class MaterialTransactionController {

    private final MaterialTransactionService materialTransactionService;

    private final MaterialTransactionMapper materialTransactionMapper;

    @GetMapping
    @ApiOperation(value = "Lấy danh sách giao dịch vật liệu")
    public ResponseEntity<?> getAllMaterialTransactions(@Valid GetMaterialTransactionRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getStart(), request.getLimit());
        Page<MaterialTransaction> page = materialTransactionService.getAllMaterialTransaction(request, pageRequest);
        return BaseResponse.successListData(page.getContent().stream()
                .map(materialTransactionMapper::toMaterialTransactionDto)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Lấy thông tin giao dịch vật liệu")
    public ResponseEntity<?> getMaterialTransactionById(@PathVariable Long id) {
        MaterialTransactionDto transaction = materialTransactionService.selectMaterialTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/batch/{materialBatchId}")
    @ApiOperation(value = "Lấy giao dịch theo batch")
    public ResponseEntity<?> getMaterialTransactionsByBatch(@PathVariable Long materialBatchId) {
        List<MaterialTransactionDto> transactions = materialTransactionService.selectMaterialTransactionsByBatch(materialBatchId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/type/{transactionType}")
    @ApiOperation(value = "Lấy giao dịch theo loại")
    public ResponseEntity<?> getMaterialTransactionsByType(@PathVariable MaterialTransaction.TransactionType transactionType) {
        List<MaterialTransactionDto> transactions = materialTransactionService.selectMaterialTransactionsByType(transactionType);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/date-range")
    @ApiOperation(value = "Lấy giao dịch theo khoảng thời gian")
    public ResponseEntity<?> getMaterialTransactionsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<MaterialTransactionDto> transactions = materialTransactionService.selectMaterialTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/warehouse/{warehouseId}/date-range")
    @ApiOperation(value = "Lấy danh sách giao dịch của một kho trong khoảng thời gian")
    public ResponseEntity<?> getWarehouseTransactionsByDateRange(
            @PathVariable Long warehouseId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<MaterialTransactionDto> transactions = materialTransactionService.getWarehouseTransactionsByDateRange(warehouseId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/batch/{materialBatchId}/quantity/{transactionType}")
    @ApiOperation(value = "Lấy tổng số lượng theo batch và loại giao dịch")
    public ResponseEntity<?> getTotalQuantityByBatchAndType(
            @PathVariable Long materialBatchId,
            @PathVariable MaterialTransaction.TransactionType transactionType) {
        Double totalQuantity = materialTransactionService.getTotalQuantityByBatchAndType(materialBatchId, transactionType);
        return ResponseEntity.ok(totalQuantity);
    }

    @PostMapping
    @ApiOperation(value = "Tạo một giao dịch vật liệu mới")
    public ResponseEntity<?> createMaterialTransaction(@Valid @RequestBody CreateMaterialTransactionRequest request) {
        MaterialTransactionDto transaction = materialTransactionService.createMaterialTransaction(request);
        return ResponseEntity.ok(transaction);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Cập nhật thông tin giao dịch vật liệu")
    public ResponseEntity<?> updateMaterialTransaction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMaterialTransactionRequest request) {
        request.setId(id);
        MaterialTransactionDto transaction = materialTransactionService.updateMaterialTransaction(request);
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Xóa giao dịch vật liệu")
    public ResponseEntity<?> deleteMaterialTransaction(@PathVariable Long id) {
        materialTransactionService.deleteMaterialTransaction(id);
        return ResponseEntity.ok("Xoá thành công " + id);
    }

    @DeleteMapping("/batch")
    @ApiOperation(value = "Xóa danh sách giao dịch")
    public ResponseEntity<?> deleteAllIdMaterialTransactions(@RequestBody List<Long> ids) {
        List<MaterialTransactionDto> deletedTransactions = materialTransactionService.deleteAllIdMaterialTransactions(ids);
        return ResponseEntity.ok(deletedTransactions);
    }

    @PutMapping("/{id}/approve")
    @ApiOperation(value = "Phê duyệt giao dịch")
    public ResponseEntity<?> approveMaterialTransaction(
            @PathVariable Long id,
            @RequestParam Long approvedById) {
        materialTransactionService.approveMaterialTransaction(id, approvedById);
        return ResponseEntity.ok("Phê duyệt thành công " + id);
    }
}
