package ext.vnua.veterinary_beapp.modules.productionplan.mapper;

import ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionPlanDto;
import ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionPlan;
import ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionPlanProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductionPlanMapper {

    @Mapping(source = "formula.id", target = "formulaId")
    @Mapping(source = "formula.version", target = "formulaVersion")
    @Mapping(source = "formula.header.formulaCode", target = "formulaCode")
    @Mapping(source = "formula.header.formulaName", target = "formulaName")
    @Mapping(source = "lot.lotNumber", target = "lotNumber")
    @Mapping(source = "lot.sequenceInMonth", target = "sequenceInMonth")
    @Mapping(source = "lot.planMonth", target = "planMonth")
    @Mapping(source = "lot.planYear", target = "planYear")
    @Mapping(source = "lot.planDate", target = "planDate")
    @Mapping(source = "productLines", target = "products")
    ProductionPlanDto toDto(ProductionPlan plan);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productCode", target = "productCode")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "productionCostSheet.id", target = "productionCostSheetId")
    ProductionPlanDto.ProductLineDto toProductLineDto(ProductionPlanProduct product);

    List<ProductionPlanDto.ProductLineDto> toProductLineDtos(List<ProductionPlanProduct> products);
}
