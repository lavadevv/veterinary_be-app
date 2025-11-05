package ext.vnua.veterinary_beapp.modules.production.mapper;

import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderMaterialDto;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrderMaterial;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductionOrderMaterialMapper {

    @Mapping(source = "productionOrder.id", target = "productionOrderId")
    @Mapping(source = "productionOrder.orderCode", target = "orderCode")

    // MaterialBatchItem mappings
    @Mapping(source = "materialBatchItem.id", target = "materialBatchItemId")
    @Mapping(source = "materialBatchItem.internalItemCode", target = "materialBatchItemCode")
    
    // Batch info (parent của item)
    @Mapping(source = "materialBatchItem.batch.id", target = "materialBatchId")
    @Mapping(source = "materialBatchItem.batch.batchNumber", target = "materialBatchNumber")

    // Material info
    @Mapping(source = "materialBatchItem.material.id", target = "materialId")
    @Mapping(source = "materialBatchItem.material.materialCode", target = "materialCode")
    @Mapping(source = "materialBatchItem.material.materialName", target = "materialName")
    
    ProductionOrderMaterialDto toDto(ProductionOrderMaterial entity);
}
