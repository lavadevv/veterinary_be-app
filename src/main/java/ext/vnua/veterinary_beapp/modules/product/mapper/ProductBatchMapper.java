package ext.vnua.veterinary_beapp.modules.product.mapper;

import ext.vnua.veterinary_beapp.modules.product.dto.ProductBatchDto;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBatch;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductBatchMapper {
    ProductBatchDto toDto(ProductBatch batch);

    @AfterMapping
    default void enrich(@MappingTarget ProductBatchDto dto, ProductBatch src) {
        if (src.getProduct() != null) {
            dto.setProductId(src.getProduct().getId());
            dto.setProductCode(src.getProduct().getProductCode());
            dto.setProductName(src.getProduct().getProductName());
        }
        if (src.getFormula() != null) {
            dto.setFormulaId(src.getFormula().getId());
            dto.setFormulaVersion(src.getFormula().getVersion());
        }
        if (src.getQcApprovedBy() != null) {
            dto.setQcApprovedById(src.getQcApprovedBy().getId());
        }

        if (src.getLocation() != null) {
            dto.setLocationId(src.getLocation().getId());
            dto.setLocationCode(src.getLocation().getLocationCode());
        }
    }
}