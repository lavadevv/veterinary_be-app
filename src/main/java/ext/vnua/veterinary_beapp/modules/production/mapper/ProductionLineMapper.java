package ext.vnua.veterinary_beapp.modules.production.mapper;

import ext.vnua.veterinary_beapp.modules.production.dto.ProductionLineDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionLine.CreateProductionLineRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionLine.UpdateProductionLineRequest;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionLine;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductionLineMapper {

    ProductionLineDto toDto(ProductionLine entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true) // Will be set in service
    ProductionLine toCreate(CreateProductionLineRequest req);

    @Mapping(target = "id", ignore = true) // Will be set in service
    ProductionLine toUpdate(UpdateProductionLineRequest req);

    /**
     * Update existing entity from request, ignoring null fields
     * This preserves audit fields and other system fields
     */
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(UpdateProductionLineRequest req, @MappingTarget ProductionLine existingEntity);
}