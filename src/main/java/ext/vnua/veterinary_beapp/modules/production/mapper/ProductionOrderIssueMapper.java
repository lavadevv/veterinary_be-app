package ext.vnua.veterinary_beapp.modules.production.mapper;

import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderIssueDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionOrderIssue.CreateProductionOrderIssueRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionOrderIssue.UpdateProductionOrderIssueRequest;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrderIssue;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductionOrderIssueMapper {

    @Mapping(source = "productionOrder.id", target = "productionOrderId")
    @Mapping(source = "productionOrder.orderCode", target = "orderCode")
    @Mapping(source = "approvedBy.fullName", target = "approvedBy")
    ProductionOrderIssueDto toDto(ProductionOrderIssue entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productionOrder", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "status", expression = "java(ext.vnua.veterinary_beapp.modules.production.enums.IssueStatus.PENDING)")
    ProductionOrderIssue toCreate(CreateProductionOrderIssueRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(UpdateProductionOrderIssueRequest request, @MappingTarget ProductionOrderIssue entity);
}
