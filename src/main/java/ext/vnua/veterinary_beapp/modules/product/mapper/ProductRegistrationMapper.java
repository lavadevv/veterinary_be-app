package ext.vnua.veterinary_beapp.modules.product.mapper;

import ext.vnua.veterinary_beapp.modules.product.dto.ProductRegistrationDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration.CreateProductRegistrationRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration.UpdateProductRegistrationRequest;
import ext.vnua.veterinary_beapp.modules.product.model.ProductRegistration;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductRegistrationMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productCode", target = "productCode")
    @Mapping(source = "product.productName", target = "productName")
    ProductRegistrationDto toDto(ProductRegistration entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductRegistration toCreate(CreateProductRegistrationRequest request);

    @Mapping(target = "product", ignore = true)
    void updateEntity(UpdateProductRegistrationRequest request, @MappingTarget ProductRegistration entity);
}