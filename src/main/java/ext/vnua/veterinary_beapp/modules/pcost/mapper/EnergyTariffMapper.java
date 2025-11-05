package ext.vnua.veterinary_beapp.modules.pcost.mapper;

import ext.vnua.veterinary_beapp.modules.pcost.dto.EnergyTariffDto;
import ext.vnua.veterinary_beapp.modules.pcost.model.EnergyTariff;
import org.hibernate.Hibernate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EnergyTariffMapper {
    
    @Mapping(target = "unitOfMeasureId", expression = "java(getUnitOfMeasureId(entity))")
    @Mapping(target = "unitOfMeasureName", expression = "java(getUnitOfMeasureName(entity))")
    EnergyTariffDto toDto(EnergyTariff entity);
    
    @Mapping(target = "unitOfMeasure", ignore = true)
    EnergyTariff toEntity(EnergyTariffDto dto);
    
    // Helper methods để xử lý lazy loading an toàn
    default Long getUnitOfMeasureId(EnergyTariff entity) {
        if (entity == null || entity.getUnitOfMeasure() == null) {
            return null;
        }
        if (!Hibernate.isInitialized(entity.getUnitOfMeasure())) {
            return null;
        }
        return entity.getUnitOfMeasure().getId();
    }
    
    default String getUnitOfMeasureName(EnergyTariff entity) {
        if (entity == null || entity.getUnitOfMeasure() == null) {
            return null;
        }
        if (!Hibernate.isInitialized(entity.getUnitOfMeasure())) {
            return null;
        }
        return entity.getUnitOfMeasure().getName();
    }
}
