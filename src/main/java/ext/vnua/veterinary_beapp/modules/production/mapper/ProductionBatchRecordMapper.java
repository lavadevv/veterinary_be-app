package ext.vnua.veterinary_beapp.modules.production.mapper;

import ext.vnua.veterinary_beapp.modules.production.dto.ProductionBatchRecordDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionBatchRecord.CreateProductionBatchRecordRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionBatchRecord.UpdateProductionBatchRecordRequest;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionBatchRecord;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductionBatchRecordMapper {

    @Mapping(source = "productionOrder.id", target = "productionOrderId")
    @Mapping(source = "productionOrder.orderCode", target = "orderCode")
    @Mapping(source = "approvedBy.id", target = "approvedById")
    @Mapping(source = "approvedBy.fullName", target = "approvedByName")
    @Mapping(source = "status", target = "status")
    ProductionBatchRecordDto toDto(ProductionBatchRecord entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productionOrder", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedDate", ignore = true)
    @Mapping(target = "status", ignore = true) // default PENDING
    ProductionBatchRecord toCreate(CreateProductionBatchRecordRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedDate", ignore = true)
    void updateFromRequest(UpdateProductionBatchRecordRequest request,
                           @MappingTarget ProductionBatchRecord entity);
}
