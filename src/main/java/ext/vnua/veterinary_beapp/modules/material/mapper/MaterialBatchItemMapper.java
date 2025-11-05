package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchItemDto;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MaterialBatchItemMapper {

    @Mapping(target = "batchId", source = "batch.id")
    @Mapping(target = "materialId", source = "material.id")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "manufacturerId", source = "manufacturer.id")
    @Mapping(target = "materialName", source = "material.materialName")
    @Mapping(target = "materialCode", source = "material.materialCode")
    @Mapping(target = "internationalName", source = "material.internationalName")
    @Mapping(target = "unitOfMeasure", source = "material.unitOfMeasure.name")
    @Mapping(target = "locationCode", source = "location.locationCode")
    @Mapping(target = "locationShelf", source = "location.shelf")
    @Mapping(target = "supplierName", source = "supplier.supplierName")
    @Mapping(target = "manufacturerName", source = "manufacturer.manufacturerName")
    @Mapping(target = "activeIngredientIds", expression = "java(item.getBatchItemActiveIngredients() != null ? item.getBatchItemActiveIngredients().stream().map(ai -> ai.getId()).collect(java.util.stream.Collectors.toList()) : null)")
    @Mapping(target = "totalActiveIngredientsCount", expression = "java(item.getBatchItemActiveIngredients() != null ? item.getBatchItemActiveIngredients().size() : 0)")
    MaterialBatchItemDto toDto(MaterialBatchItem item);

    List<MaterialBatchItemDto> toDtoList(List<MaterialBatchItem> items);
}
