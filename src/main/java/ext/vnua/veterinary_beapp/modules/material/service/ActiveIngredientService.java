package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.ActiveIngredientDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.CreateActiveIngredientRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.GetActiveIngredientRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.UpdateActiveIngredientRequest;
import ext.vnua.veterinary_beapp.modules.material.model.ActiveIngredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ActiveIngredientService {
    
    Page<ActiveIngredient> search(GetActiveIngredientRequest request, Pageable pageable);
    
    List<ActiveIngredientDto> getActiveIngredients();
    
    ActiveIngredientDto getActiveIngredientById(Long id);
    
    ActiveIngredientDto getActiveIngredientByCode(String code);
    
    ActiveIngredientDto createActiveIngredient(CreateActiveIngredientRequest request);
    
    ActiveIngredientDto updateActiveIngredient(UpdateActiveIngredientRequest request);
    
    void deleteActiveIngredient(Long id);
    
    void toggleActiveStatus(Long id);
    
    List<ActiveIngredientDto> searchByKeyword(String keyword);
}