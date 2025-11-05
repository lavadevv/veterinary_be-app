package ext.vnua.veterinary_beapp.modules.product.mapper;

import ext.vnua.veterinary_beapp.modules.product.dto.response.ProductBrandDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productbrand.UpsertProductBrandRequest;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBrand;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ProductBrandMapper {

    ProductBrandMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(ProductBrandMapper.class);

    // Entity -> DTO
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productCode", target = "productCode")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "brand.id", target = "brandId")
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "productionCostSheet.id", target = "productionCostSheetId")
    @Mapping(source = "productionCostSheet.sheetCode", target = "productionCostSheetCode")
    @Mapping(source = "productionCostSheet.sheetName", target = "productionCostSheetName")
    ProductBrandDto toDto(ProductBrand entity);

    // Request -> Entity (for create)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "productionCostSheet", ignore = true)
    @Mapping(target = "sellingPrice", ignore = true) // Auto-calculated
    ProductBrand toEntity(UpsertProductBrandRequest request);

    // Update entity from request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "productionCostSheet", ignore = true)
    @Mapping(target = "sellingPrice", ignore = true) // Auto-calculated
    void updateEntityFromRequest(UpsertProductBrandRequest request, @MappingTarget ProductBrand entity);

    // AfterMapping để set giá trị mặc định
    @AfterMapping
    default void afterMappingCreate(@MappingTarget ProductBrand entity, UpsertProductBrandRequest request) {
        if (entity.getMaterialCost() == null) {
            entity.setMaterialCost(BigDecimal.ZERO);
        }
        if (entity.getProductionUnitCost() == null) {
            entity.setProductionUnitCost(BigDecimal.ZERO);
        }
        if (entity.getProfitMarginPercentage() == null) {
            entity.setProfitMarginPercentage(BigDecimal.ZERO);
        }
        if (entity.getVatPercentage() == null) {
            entity.setVatPercentage(BigDecimal.ZERO);
        }
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }
    }
}
