package ext.vnua.veterinary_beapp.modules.product.mapper;

import ext.vnua.veterinary_beapp.modules.product.dto.ProductFormulaDto;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductFormulaItemDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.UpsertFormulaRequest;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormulaItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductFormulaMapper {

    // Ignore Header fields - will be populated in @AfterMapping
    @Mapping(target = "formulaCode", ignore = true)
    @Mapping(target = "formulaName", ignore = true)
    @Mapping(target = "headerDescription", ignore = true)
    @Mapping(target = "appliedProducts", ignore = true)
    // Ignore representative product fields - will be populated in @AfterMapping
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "productCode", ignore = true)
    @Mapping(target = "productName", ignore = true)
    // Map audit fields
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "lastModifiedDate", target = "lastModifiedDate")
    @Mapping(source = "lastModifiedBy", target = "lastModifiedBy")
    @Mapping(source = "approvedBy.id", target = "approvedById")
    @Mapping(source = "isLiquidFormula", target = "isLiquidFormula")
    ProductFormulaDto toDto(ProductFormula entity);

    @Mapping(source = "material.id", target = "materialId")
    @Mapping(source = "material.materialCode", target = "materialCode")
    @Mapping(source = "material.materialName", target = "materialName")
    ProductFormulaItemDto toItemDto(ProductFormulaItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "formulaItems", ignore = true)
    @Mapping(target = "header", ignore = true)
    ProductFormula toEntity(UpsertFormulaRequest request);

    default Product pickFirstProduct(ext.vnua.veterinary_beapp.modules.product.model.ProductFormula f) {
        if (f == null || f.getHeader() == null || f.getHeader().getProducts() == null) return null;
        return f.getHeader().getProducts().stream().findFirst().orElse(null);
    }

    @AfterMapping
    default void fillHeaderAndProducts(ProductFormula src, @MappingTarget ProductFormulaDto tgt) {
        // Fill Header fields
        if (src.getHeader() != null) {
            tgt.setFormulaCode(src.getHeader().getFormulaCode());
            tgt.setFormulaName(src.getHeader().getFormulaName());
            tgt.setHeaderDescription(src.getHeader().getDescription());
            
            // Fill appliedProducts (all products linked to this header)
            if (src.getHeader().getProducts() != null && !src.getHeader().getProducts().isEmpty()) {
                var appliedProducts = src.getHeader().getProducts().stream()
                    .map(p -> {
                        var ap = new ProductFormulaDto.AppliedProduct();
                        ap.setId(p.getId());
                        ap.setProductCode(p.getProductCode());
                        ap.setProductName(p.getProductName());
                        return ap;
                    })
                    .toList();
                tgt.setAppliedProducts(appliedProducts);
            }
        }
        
        // Fill representative product (first product for backward compatibility)
        Product rep = pickFirstProduct(src);
        if (rep != null) {
            tgt.setProductId(rep.getId());
            tgt.setProductCode(rep.getProductCode());
            tgt.setProductName(rep.getProductName());
        }
    }

}
