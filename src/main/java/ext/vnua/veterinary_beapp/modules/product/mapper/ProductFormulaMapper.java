package ext.vnua.veterinary_beapp.modules.product.mapper;

import ext.vnua.veterinary_beapp.modules.product.dto.ProductFormulaDto;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductFormulaItemDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.UpsertFormulaRequest;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormulaItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductFormulaMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productCode", target = "productCode")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "approvedBy.id", target = "approvedById")
    ProductFormulaDto toDto(ProductFormula entity);

    @Mapping(source = "material.id", target = "materialId")
    @Mapping(source = "material.materialCode", target = "materialCode")
    @Mapping(source = "material.materialName", target = "materialName")
    ProductFormulaItemDto toItemDto(ProductFormulaItem item);

    // Upsert: map từ request sang entity (bỏ map product/createdBy/approvedBy để set trong service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "formulaItems", ignore = true)
    ProductFormula toEntity(UpsertFormulaRequest request);
}