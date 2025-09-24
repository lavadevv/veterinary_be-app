package ext.vnua.veterinary_beapp.modules.product.mapper;

import ext.vnua.veterinary_beapp.modules.product.dto.ProductBatchConsumptionDto;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBatchConsumption;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductBatchConsumptionMapper {
    @Mapping(source = "productBatch.id", target = "productBatchId")
    @Mapping(source = "productBatch.batchNumber", target = "productBatchNumber")
    @Mapping(source = "materialBatch.id", target = "materialBatchId")
    @Mapping(source = "materialBatch.material.materialCode", target = "materialCode")
    @Mapping(source = "materialBatch.batchNumber", target = "materialBatchNumber")
    ProductBatchConsumptionDto toDto(ProductBatchConsumption entity);
}