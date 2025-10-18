package ext.vnua.veterinary_beapp.modules.product.services;

import ext.vnua.veterinary_beapp.modules.product.dto.ProductRegistrationDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration.CreateProductRegistrationRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration.GetProductRegistrationRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration.UpdateProductRegistrationRequest;
import ext.vnua.veterinary_beapp.modules.product.model.ProductRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ProductRegistrationService {
    Page<ProductRegistration> filter(GetProductRegistrationRequest req, PageRequest pr);
    ProductRegistrationDto getByProduct(Long productId);
    ProductRegistrationDto create(CreateProductRegistrationRequest req);
    ProductRegistrationDto update(UpdateProductRegistrationRequest req);
    void delete(Long id);
}