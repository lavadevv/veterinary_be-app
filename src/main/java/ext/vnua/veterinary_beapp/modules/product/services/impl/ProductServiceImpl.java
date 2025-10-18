package ext.vnua.veterinary_beapp.modules.product.services.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.product.CreateProductRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.product.UpdateProductRequest;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductMapper;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.custom.CustomProductQuery;
import ext.vnua.veterinary_beapp.modules.product.services.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<Product> getAllProducts(CustomProductQuery.ProductFilterParam param, PageRequest pageRequest) {
        Specification<Product> specification = CustomProductQuery.getFilterProduct(param);
        return productRepository.findAll(specification, pageRequest);
    }

    @Override
    public ProductDto selectProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Sản phẩm không tồn tại"));
        return productMapper.toProductDto(product);
    }

    @Override
    public ProductDto selectProductByCode(String productCode) {
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new DataExistException("Mã sản phẩm không tồn tại"));
        return productMapper.toProductDto(product);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "Product", description = "Tạo mới sản phẩm")
    public ProductDto createProduct(CreateProductRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new DataExistException("Mã sản phẩm đã tồn tại");
        }

        Product product = productMapper.toCreateProduct(request);

        return productMapper.toProductDto(productRepository.saveAndFlush(product));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "Product", description = "Cập nhật sản phẩm")
    public ProductDto updateProduct(UpdateProductRequest request) {
        Product existing = productRepository.findById(request.getId())
                .orElseThrow(() -> new DataExistException("Sản phẩm không tồn tại"));

        // Kiểm tra mã sản phẩm trùng
        if (!existing.getProductCode().equals(request.getProductCode()) &&
                productRepository.existsByProductCode(request.getProductCode())) {
            throw new DataExistException("Mã sản phẩm đã tồn tại");
        }

        Product updated = productMapper.toUpdateProduct(request);
        updated.setId(existing.getId());

        return productMapper.toProductDto(productRepository.saveAndFlush(updated));
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Product", description = "Xóa sản phẩm")
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new DataExistException("Sản phẩm không tồn tại");
        }
        productRepository.deleteById(id);
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Product", description = "Xóa danh sách sản phẩm")
    public List<ProductDto> deleteAllProducts(List<Long> ids) {
        List<ProductDto> deletedProducts = new ArrayList<>();
        for (Long id : ids) {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new MyCustomException("Sản phẩm không tồn tại: " + id));
            deletedProducts.add(productMapper.toProductDto(product));
            productRepository.delete(product);
        }
        return deletedProducts;
    }

    @Override
    public List<ProductDto> getProductsByCategory(String category) {
        CustomProductQuery.ProductFilterParam param = new CustomProductQuery.ProductFilterParam();
        param.setCategory(category);
        return productRepository.findAll(CustomProductQuery.getFilterProduct(param)).stream()
                .map(productMapper::toProductDto)
                .toList();
    }

    @Override
    @Transactional
    public void updateStock(Long productId, Double newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DataExistException("Sản phẩm không tồn tại"));

        if (newStock < 0) {
            throw new MyCustomException("Tồn kho không được âm");
        }
        product.setCurrentStock(newStock);

        productRepository.saveAndFlush(product);
    }

    @Override
    @Transactional
    public void toggleActiveStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DataExistException("Sản phẩm không tồn tại"));

        product.setIsActive(!product.getIsActive());
        productRepository.saveAndFlush(product);
    }

}
