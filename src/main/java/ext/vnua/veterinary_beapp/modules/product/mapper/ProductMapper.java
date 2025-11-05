package ext.vnua.veterinary_beapp.modules.product.mapper;


import ext.vnua.veterinary_beapp.modules.product.dto.ProductDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.product.CreateProductRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.product.UpdateProductRequest;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(ProductMapper.class);

    // Entity -> DTO
    ProductDto toProductDto(Product product);

    // Create request -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "batches", ignore = true)
    @Mapping(target = "registration", ignore = true)
    Product toCreateProduct(CreateProductRequest request);

    // Update request -> Entity
    @Mapping(target = "batches", ignore = true)
    @Mapping(target = "registration", ignore = true)
    Product toUpdateProduct(UpdateProductRequest request);

    // Update entity in-place từ request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "batches", ignore = true)
    @Mapping(target = "registration", ignore = true)
    void updateProductFromRequest(UpdateProductRequest request, @MappingTarget Product product);

    // AfterMapping để set giá trị mặc định
    @AfterMapping
    default void afterMappingCreate(@MappingTarget Product product, CreateProductRequest request) {
        if (product.getCurrentStock() == null) {
            product.setCurrentStock(0.0);
        }
        if (product.getRequiresColdStorage() == null) {
            product.setRequiresColdStorage(false);
        }
        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }
    }

    @AfterMapping
    default void afterMappingUpdate(@MappingTarget Product product, UpdateProductRequest request) {
        if (product.getCurrentStock() == null) {
            product.setCurrentStock(0.0);
        }
    }
}
