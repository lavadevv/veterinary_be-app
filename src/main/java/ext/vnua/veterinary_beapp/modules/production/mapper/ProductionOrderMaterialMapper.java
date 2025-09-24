package ext.vnua.veterinary_beapp.modules.production.mapper;

import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderMaterialDto;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrderMaterial;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductionOrderMaterialMapper {

    @Mapping(source = "productionOrder.id", target = "productionOrderId")
    @Mapping(source = "productionOrder.orderCode", target = "orderCode")

    @Mapping(source = "materialBatch.id", target = "materialBatchId")
    @Mapping(source = "materialBatch.batchNumber", target = "materialBatchNumber")

    @Mapping(source = "materialBatch.material.id", target = "materialId")
    @Mapping(source = "materialBatch.material.materialCode", target = "materialCode")
    @Mapping(source = "materialBatch.material.materialName", target = "materialName")
    ProductionOrderMaterialDto toDto(ProductionOrderMaterial entity);
}
