package ext.vnua.veterinary_beapp.modules.product.mapper;

import ext.vnua.veterinary_beapp.modules.product.dto.ProductBatchConsumptionDto;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBatchConsumption;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductBatchConsumptionMapper {
    @Mapping(source = "productBatch.id", target = "productBatchId")
    @Mapping(source = "productBatch.batchNumber", target = "productBatchNumber")
    
    // MaterialBatchItem mappings
    @Mapping(source = "materialBatchItem.id", target = "materialBatchItemId")
    @Mapping(source = "materialBatchItem.material.materialCode", target = "materialCode")
    @Mapping(source = "materialBatchItem.internalItemCode", target = "materialBatchItemCode")
    
    // Parent batch info
    @Mapping(source = "materialBatchItem.batch.id", target = "materialBatchId")
    @Mapping(source = "materialBatchItem.batch.batchNumber", target = "materialBatchNumber")
    
    ProductBatchConsumptionDto toDto(ProductBatchConsumption entity);
}