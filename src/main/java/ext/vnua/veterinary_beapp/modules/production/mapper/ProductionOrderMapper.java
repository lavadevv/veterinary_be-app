package ext.vnua.veterinary_beapp.modules.production.mapper;

import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderDto;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductionOrderMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productCode", target = "productCode")
    @Mapping(source = "product.productName", target = "productName")

    @Mapping(source = "productionLine.id", target = "productionLineId")
    @Mapping(source = "productionLine.lineCode", target = "productionLineCode")
    @Mapping(source = "productionLine.name", target = "productionLineName")

    @Mapping(source = "createdByUser.id", target = "createdById")
    @Mapping(source = "createdByUser.fullName", target = "createdByName")

    @Mapping(source = "approvedByUser.id", target = "approvedById")
    @Mapping(source = "approvedByUser.fullName", target = "approvedByName")
    ProductionOrderDto toDto(ProductionOrder entity);

    @InheritInverseConfiguration
    ProductionOrder toEntity(ProductionOrderDto dto);
}
