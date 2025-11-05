package ext.vnua.veterinary_beapp.modules.pcost.mapper;

import ext.vnua.veterinary_beapp.modules.pcost.dto.LaborRateDto;
import ext.vnua.veterinary_beapp.modules.pcost.model.LaborRate;
import org.hibernate.Hibernate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LaborRateMapper {
    
    @Mapping(target = "unitOfMeasureId", expression = "java(getUnitOfMeasureId(entity))")
    @Mapping(target = "unitOfMeasureName", expression = "java(getUnitOfMeasureName(entity))")
    LaborRateDto toDto(LaborRate entity);
    
    @Mapping(target = "unitOfMeasure", ignore = true)
    LaborRate toEntity(LaborRateDto dto);
    
    // Helper methods để xử lý lazy loading an toàn
    default Long getUnitOfMeasureId(LaborRate entity) {
        if (entity == null || entity.getUnitOfMeasure() == null) {
            return null;
        }
        if (!Hibernate.isInitialized(entity.getUnitOfMeasure())) {
            return null;
        }
        return entity.getUnitOfMeasure().getId();
    }
    
    default String getUnitOfMeasureName(LaborRate entity) {
        if (entity == null || entity.getUnitOfMeasure() == null) {
            return null;
        }
        if (!Hibernate.isInitialized(entity.getUnitOfMeasure())) {
            return null;
        }
        return entity.getUnitOfMeasure().getName();
    }
}
