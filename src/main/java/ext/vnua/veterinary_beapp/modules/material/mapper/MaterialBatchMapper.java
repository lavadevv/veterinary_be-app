package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchDto;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {MaterialMapper.class, LocationMapper.class})
public interface MaterialBatchMapper {
    MaterialBatchMapper INSTANCE = Mappers.getMapper(MaterialBatchMapper.class);

    @Mapping(source = "material", target = "materialDto")
    @Mapping(source = "location", target = "locationDto")
    MaterialBatchDto toMaterialBatchDto(MaterialBatch materialBatch);

    @Mapping(source = "materialDto", target = "material")
    @Mapping(source = "locationDto", target = "location")
    MaterialBatch toMaterialBatch(MaterialBatchDto materialBatchDto);
}