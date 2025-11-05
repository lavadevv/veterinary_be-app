
package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchItemActiveIngredientDto;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItemActiveIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MaterialBatchItemActiveIngredientMapper {

    @Mapping(target = "batchItemId", source = "batchItem.id")
    @Mapping(target = "activeIngredientId", source = "activeIngredient.id")
    @Mapping(target = "activeIngredientName", source = "activeIngredient.ingredientName")
    @Mapping(target = "activeIngredientCode", source = "activeIngredient.ingredientCode")
    @Mapping(target = "materialContentValue", expression = "java(getMaterialContentValue(ingredient))")
    @Mapping(target = "materialContentUnit", expression = "java(getMaterialContentUnit(ingredient))")
    @Mapping(target = "isQualified", expression = "java(ingredient.isQualified())")
    @Mapping(target = "ratioPercentage", expression = "java(ingredient.getRatioPercentage())")
    @Mapping(target = "deviationPercentage", expression = "java(ingredient.getDeviationPercentage())") // Deprecated but kept for backward compatibility
    MaterialBatchItemActiveIngredientDto toDto(MaterialBatchItemActiveIngredient ingredient);

    List<MaterialBatchItemActiveIngredientDto> toDtoList(List<MaterialBatchItemActiveIngredient> ingredients);
    
    /**
     * Get content value from MaterialActiveIngredient master data
     */
    default java.math.BigDecimal getMaterialContentValue(MaterialBatchItemActiveIngredient ingredient) {
        if (ingredient == null || ingredient.getBatchItem() == null || 
            ingredient.getBatchItem().getMaterial() == null ||
            ingredient.getActiveIngredient() == null) {
            return null;
        }
        
        var material = ingredient.getBatchItem().getMaterial();
        var activeIngredient = ingredient.getActiveIngredient();
        
        // Find MaterialActiveIngredient for this material and active ingredient
        return material.getActiveIngredients().stream()
            .filter(mai -> mai.getActiveIngredient().getId().equals(activeIngredient.getId()))
            .findFirst()
            .map(ext.vnua.veterinary_beapp.modules.material.model.MaterialActiveIngredient::getContentValue)
            .orElse(null);
    }
    
    /**
     * Get content unit from MaterialActiveIngredient master data
     */
    default String getMaterialContentUnit(MaterialBatchItemActiveIngredient ingredient) {
        if (ingredient == null || ingredient.getBatchItem() == null || 
            ingredient.getBatchItem().getMaterial() == null ||
            ingredient.getActiveIngredient() == null) {
            return null;
        }
        
        var material = ingredient.getBatchItem().getMaterial();
        var activeIngredient = ingredient.getActiveIngredient();
        
        // Find MaterialActiveIngredient for this material and active ingredient
        return material.getActiveIngredients().stream()
            .filter(mai -> mai.getActiveIngredient().getId().equals(activeIngredient.getId()))
            .findFirst()
            .map(ext.vnua.veterinary_beapp.modules.material.model.MaterialActiveIngredient::getContentUnit)
            .orElse(null);
    }
}
