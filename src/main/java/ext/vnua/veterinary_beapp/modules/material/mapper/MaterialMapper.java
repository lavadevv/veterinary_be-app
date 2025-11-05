// File: ext/vnua/veterinary_beapp/modules/material/mapper/MaterialMapper.java
package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.CreateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.UpdateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialActiveIngredient;
import org.hibernate.Hibernate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = {SupplierMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MaterialMapper {

    // ============== ENTITY -> DTO ==============
    @Mapping(source = "supplier", target = "supplierDto")
    @Mapping(target = "unitOfMeasureId", source = "unitOfMeasure.id")
    @Mapping(target = "unitOfMeasureName", source = "unitOfMeasure.name")
    @Mapping(target = "materialCategoryId", source = "materialCategory.id")
    @Mapping(target = "materialCategoryName", source = "materialCategory.categoryName")
    @Mapping(target = "materialFormTypeId", source = "materialFormType.id")
    @Mapping(target = "materialFormTypeName", source = "materialFormType.name")
    @Mapping(target = "activeIngredients", expression = "java(mapActiveIngredientsIfInitialized(material))")
    @Mapping(target = "activeIngredientsCount", expression = "java(countActiveIngredients(material))")
    MaterialDto toMaterialDto(Material material);

    // (Chiều DTO -> Entity không khuyến nghị; giữ để tương thích)
    @Mapping(source = "supplierDto", target = "supplier")
    @Mapping(target = "unitOfMeasure", ignore = true)
    @Mapping(target = "materialCategory", ignore = true)
    @Mapping(target = "materialFormType", ignore = true)
    Material toMaterial(MaterialDto materialDto);

    // ============== CREATE DTO -> ENTITY ==============
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "batchItems", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "unitOfMeasure", ignore = true)
    @Mapping(target = "materialCategory", ignore = true)
    @Mapping(target = "materialFormType", ignore = true)
    Material toCreateMaterial(CreateMaterialRequest request);

    // ============== UPDATE DTO -> ENTITY (ít dùng) ==============
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "batchItems", ignore = true)
    @Mapping(target = "unitOfMeasure", ignore = true)
    @Mapping(target = "materialCategory", ignore = true)
    @Mapping(target = "materialFormType", ignore = true)
    Material toUpdateMaterial(UpdateMaterialRequest request);

    // ============== UPDATE IN-PLACE ==============
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "batchItems", ignore = true)
    @Mapping(target = "unitOfMeasure", ignore = true)
    @Mapping(target = "materialCategory", ignore = true)
    @Mapping(target = "materialFormType", ignore = true)
    void updateMaterialFromRequest(UpdateMaterialRequest request, @MappingTarget Material material);

    @AfterMapping
    default void afterMappingCreate(@MappingTarget Material material, CreateMaterialRequest request) {
        if (material.getCurrentStock() == null) {
            material.setCurrentStock(BigDecimal.ZERO);
        }
        if (material.getIsActive() == null) {
            material.setIsActive(true);
        }
        if (material.getRequiresColdStorage() == null) {
            material.setRequiresColdStorage(false);
        }
    }

    @AfterMapping
    default void afterMappingUpdate(@MappingTarget Material material, UpdateMaterialRequest request) {
        if (material.getCurrentStock() == null) {
            material.setCurrentStock(BigDecimal.ZERO);
        }
        if (material.getRequiresColdStorage() == null) {
            material.setRequiresColdStorage(false);
        }
    }

    // Helper method to safely map activeIngredients if initialized
    default List<MaterialDto.ActiveIngredientLine> mapActiveIngredientsIfInitialized(Material material) {
        if (material == null || !Hibernate.isInitialized(material.getActiveIngredients())) {
            return Collections.emptyList();
        }
        
        return material.getActiveIngredients().stream()
                .map(this::mapActiveIngredient)
                .collect(Collectors.toList());
    }

    // Helper method to count activeIngredients
    default Integer countActiveIngredients(Material material) {
        if (material == null || !Hibernate.isInitialized(material.getActiveIngredients())) {
            return 0;
        }
        return material.getActiveIngredients() != null ? material.getActiveIngredients().size() : 0;
    }

    // Helper method to map single activeIngredient
    default MaterialDto.ActiveIngredientLine mapActiveIngredient(MaterialActiveIngredient mai) {
        if (mai == null) {
            return null;
        }
        
        MaterialDto.ActiveIngredientLine line = new MaterialDto.ActiveIngredientLine();
        line.setContentValue(mai.getContentValue());
        line.setContentUnit(mai.getContentUnit());
        
        if (mai.getActiveIngredient() != null) {
            line.setIngredientId(mai.getActiveIngredient().getId());
            line.setIngredientName(mai.getActiveIngredient().getIngredientName());
        }
        
        return line;
    }
}
