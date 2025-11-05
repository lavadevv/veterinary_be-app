package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.ActiveIngredientDto;
import ext.vnua.veterinary_beapp.modules.material.model.ActiveIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActiveIngredientMapper {
    
    ActiveIngredientDto toDto(ActiveIngredient activeIngredient);
    
    ActiveIngredient toEntity(ActiveIngredientDto dto);
}