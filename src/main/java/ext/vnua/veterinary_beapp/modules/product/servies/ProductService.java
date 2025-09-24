package ext.vnua.veterinary_beapp.modules.product.servies;

import ext.vnua.veterinary_beapp.modules.product.dto.ProductDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.product.CreateProductRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.product.UpdateProductRequest;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.repository.custom.CustomProductQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ProductService {
    Page<Product> getAllProducts(CustomProductQuery.ProductFilterParam param, PageRequest pageRequest);
    ProductDto selectProductById(Long id);
    ProductDto selectProductByCode(String productCode);

    ProductDto createProduct(CreateProductRequest request);
    ProductDto updateProduct(UpdateProductRequest request);

    void deleteProduct(Long id);
    List<ProductDto> deleteAllProducts(List<Long> ids);

    List<ProductDto> getProductsByCategory(String category);
    void updateStock(Long productId, Double newStock);
    void toggleActiveStatus(Long productId);
}
