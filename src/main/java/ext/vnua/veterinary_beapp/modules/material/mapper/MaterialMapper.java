package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.CreateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.UpdateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {SupplierMapper.class})
public interface MaterialMapper {
    MaterialMapper INSTANCE = Mappers.getMapper(MaterialMapper.class);

    @Mapping(source = "supplier", target = "supplierDto")
    MaterialDto toMaterialDto(Material material);

    @Mapping(source = "supplierDto", target = "supplier")
    Material toMaterial(MaterialDto materialDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "batches", ignore = true)
    @Mapping(target = "isActive", ignore = true) // Will be set to true by default in service
    Material toCreateMaterial(CreateMaterialRequest request);

    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "batches", ignore = true)
    Material toUpdateMaterial(UpdateMaterialRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "batches", ignore = true)
    void updateMaterialFromRequest(UpdateMaterialRequest request, @MappingTarget Material material);

    @AfterMapping
    default void afterMappingCreate(@MappingTarget Material material, CreateMaterialRequest request) {
        // Set default values if not provided
        if (material.getCurrentStock() == null) {
            material.setCurrentStock(0.0);
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
        // Ensure currentStock is not null
        if (material.getCurrentStock() == null) {
            material.setCurrentStock(0.0);
        }
        if (material.getRequiresColdStorage() == null) {
            material.setRequiresColdStorage(false);
        }
    }
}