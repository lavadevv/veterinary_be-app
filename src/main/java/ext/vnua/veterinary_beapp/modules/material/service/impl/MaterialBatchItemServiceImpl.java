package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataNotFoundException;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchItemDto;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchItemActiveIngredientDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateTestResultRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.UpdateTestResultRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.MaterialBatchItemMapper;
import ext.vnua.veterinary_beapp.modules.material.mapper.MaterialBatchItemActiveIngredientMapper;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItem;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItemActiveIngredient;
import ext.vnua.veterinary_beapp.modules.material.model.ActiveIngredient;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchItemRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchItemActiveIngredientRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.ActiveIngredientRepository;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialBatchItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaterialBatchItemServiceImpl implements MaterialBatchItemService {

    private final MaterialBatchItemRepository materialBatchItemRepository;
    private final MaterialBatchItemActiveIngredientRepository activeIngredientRepository;
    private final ActiveIngredientRepository activeIngredientMasterRepository;
    private final MaterialBatchItemMapper materialBatchItemMapper;
    private final MaterialBatchItemActiveIngredientMapper activeIngredientMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MaterialBatchItemDto> getItemsByBatchId(Long batchId) {
        List<MaterialBatchItem> items = materialBatchItemRepository.findByBatchId(batchId);
        return materialBatchItemMapper.toDtoList(items);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialBatchItemDto getItemById(Long itemId) {
        MaterialBatchItem item = materialBatchItemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException(
                        "Không tìm thấy item với ID: " + itemId
                ));
        return materialBatchItemMapper.toDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialBatchItemActiveIngredientDto> getActiveIngredientsByItemId(Long itemId) {
        // Kiểm tra item có tồn tại không
        if (!materialBatchItemRepository.existsById(itemId)) {
            throw new DataNotFoundException(
                    "Không tìm thấy item với ID: " + itemId
            );
        }
        
        List<MaterialBatchItemActiveIngredient> ingredients = 
                activeIngredientRepository.findByBatchItemId(itemId);
        return activeIngredientMapper.toDtoList(ingredients);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialBatchItemActiveIngredientDto getActiveIngredientById(Long ingredientId) {
        MaterialBatchItemActiveIngredient ingredient = activeIngredientRepository.findById(ingredientId)
                .orElseThrow(() -> new DataNotFoundException(
                        "Không tìm thấy hoạt chất với ID: " + ingredientId
                ));
        return activeIngredientMapper.toDto(ingredient);
    }

    @Override
    @Transactional
    public MaterialBatchItemActiveIngredientDto createTestResult(CreateTestResultRequest request) {
        // Kiểm tra batch item có tồn tại không
        MaterialBatchItem batchItem = materialBatchItemRepository.findById(request.getBatchItemId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Không tìm thấy batch item với ID: " + request.getBatchItemId()
                ));

        // Kiểm tra active ingredient có tồn tại không
        ActiveIngredient activeIngredient = activeIngredientMasterRepository.findById(request.getActiveIngredientId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Không tìm thấy hoạt chất với ID: " + request.getActiveIngredientId()
                ));

        // Kiểm tra xem đã có test result cho ingredient này chưa
        Optional<MaterialBatchItemActiveIngredient> existingIngredient = 
                activeIngredientRepository.findByBatchItemAndActiveIngredient(batchItem, activeIngredient);

        MaterialBatchItemActiveIngredient ingredient;
        
        if (existingIngredient.isPresent()) {
            // Nếu đã tồn tại, cập nhật thông tin
            ingredient = existingIngredient.get();
        } else {
            // Nếu chưa tồn tại, tạo mới
            ingredient = new MaterialBatchItemActiveIngredient();
            ingredient.setBatchItem(batchItem);
            ingredient.setActiveIngredient(activeIngredient);
        }

        // Cập nhật thông tin COA
        ingredient.setCoaContentValue(request.getCoaContentValue());
        ingredient.setCoaContentUnit(request.getCoaContentUnit());
        ingredient.setCoaNotes(request.getCoaNotes());

        // Cập nhật thông tin KQPT
        ingredient.setTestContentValue(request.getTestContentValue());
        ingredient.setTestContentUnit(request.getTestContentUnit());
        ingredient.setTestDate(request.getTestDate());
        ingredient.setTestMethod(request.getTestMethod());
        ingredient.setTestNotes(request.getTestNotes());

        // Lưu vào database
        MaterialBatchItemActiveIngredient saved = activeIngredientRepository.save(ingredient);
        
        return activeIngredientMapper.toDto(saved);
    }

    @Override
    @Transactional
    public MaterialBatchItemActiveIngredientDto updateTestResult(Long id, UpdateTestResultRequest request) {
        // Tìm test result
        MaterialBatchItemActiveIngredient ingredient = activeIngredientRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        "Không tìm thấy kết quả kiểm nghiệm với ID: " + id
                ));

        // Cập nhật thông tin COA nếu có
        if (request.getCoaContentValue() != null) {
            ingredient.setCoaContentValue(request.getCoaContentValue());
        }
        if (request.getCoaContentUnit() != null) {
            ingredient.setCoaContentUnit(request.getCoaContentUnit());
        }
        if (request.getCoaNotes() != null) {
            ingredient.setCoaNotes(request.getCoaNotes());
        }

        // Cập nhật thông tin KQPT nếu có
        if (request.getTestContentValue() != null) {
            ingredient.setTestContentValue(request.getTestContentValue());
        }
        if (request.getTestContentUnit() != null) {
            ingredient.setTestContentUnit(request.getTestContentUnit());
        }
        if (request.getTestDate() != null) {
            ingredient.setTestDate(request.getTestDate());
        }
        if (request.getTestMethod() != null) {
            ingredient.setTestMethod(request.getTestMethod());
        }
        if (request.getTestNotes() != null) {
            ingredient.setTestNotes(request.getTestNotes());
        }

        // Lưu vào database
        MaterialBatchItemActiveIngredient saved = activeIngredientRepository.save(ingredient);
        
        return activeIngredientMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteTestResult(Long id) {
        // Kiểm tra test result có tồn tại không
        if (!activeIngredientRepository.existsById(id)) {
            throw new DataNotFoundException(
                    "Không tìm thấy kết quả kiểm nghiệm với ID: " + id
            );
        }

        // Xóa test result
        activeIngredientRepository.deleteById(id);
    }
}
