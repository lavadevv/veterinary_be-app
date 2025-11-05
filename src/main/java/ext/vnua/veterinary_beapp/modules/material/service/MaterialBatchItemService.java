package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchItemDto;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchItemActiveIngredientDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateTestResultRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.UpdateTestResultRequest;

import java.util.List;

public interface MaterialBatchItemService {
    
    /**
     * Lấy danh sách items trong một batch
     */
    List<MaterialBatchItemDto> getItemsByBatchId(Long batchId);
    
    /**
     * Lấy thông tin chi tiết một item
     */
    MaterialBatchItemDto getItemById(Long itemId);
    
    /**
     * Lấy danh sách hoạt chất của một item
     */
    List<MaterialBatchItemActiveIngredientDto> getActiveIngredientsByItemId(Long itemId);
    
    /**
     * Lấy thông tin chi tiết một hoạt chất
     */
    MaterialBatchItemActiveIngredientDto getActiveIngredientById(Long ingredientId);
    
    /**
     * Tạo mới kết quả kiểm nghiệm (COA + KQPT)
     */
    MaterialBatchItemActiveIngredientDto createTestResult(CreateTestResultRequest request);
    
    /**
     * Cập nhật kết quả kiểm nghiệm
     */
    MaterialBatchItemActiveIngredientDto updateTestResult(Long id, UpdateTestResultRequest request);
    
    /**
     * Xóa kết quả kiểm nghiệm
     */
    void deleteTestResult(Long id);
}
