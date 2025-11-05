// mapper/UnitOfMeasureMapper.java
package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.UnitOfMeasureDto;
import ext.vnua.veterinary_beapp.modules.material.model.UnitOfMeasure;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnitOfMeasureMapper {
    UnitOfMeasureDto toDto(UnitOfMeasure entity);
}
