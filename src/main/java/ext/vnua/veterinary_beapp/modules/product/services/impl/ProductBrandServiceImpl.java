package ext.vnua.veterinary_beapp.modules.product.services.impl;

import ext.vnua.veterinary_beapp.modules.material.model.Brand;
import ext.vnua.veterinary_beapp.modules.material.repository.BrandRepository;
import ext.vnua.veterinary_beapp.modules.pcost.model.ProductionCostSheet;
import ext.vnua.veterinary_beapp.modules.pcost.repository.ProductionCostSheetRepository;
import ext.vnua.veterinary_beapp.modules.product.dto.response.ProductBrandDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productbrand.UpsertProductBrandRequest;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductBrandMapper;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBrand;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductBrandRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.custom.CustomProductBrandQuery;
import ext.vnua.veterinary_beapp.modules.product.services.ProductBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductBrandServiceImpl implements ProductBrandService {

    private final ProductBrandRepository productBrandRepository;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final ProductionCostSheetRepository productionCostSheetRepository;
    private final ProductBrandMapper productBrandMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductBrand> getAllProductBrands(CustomProductBrandQuery.ProductBrandFilterParam param, PageRequest pageRequest) {
        Specification<ProductBrand> specification = CustomProductBrandQuery.getFilterProductBrand(param);
        return productBrandRepository.findAll(specification, pageRequest);
    }

    @Override
    public ProductBrandDto create(UpsertProductBrandRequest request) {
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));
        
        // Validate brand exists
        Brand brand = brandRepository.findById(request.getBrandId())
            .orElseThrow(() -> new RuntimeException("Brand not found: " + request.getBrandId()));
        
        // Check duplicate
        if (productBrandRepository.existsByProductIdAndBrandId(request.getProductId(), request.getBrandId())) {
            throw new RuntimeException("ProductBrand already exists for this product-brand combination");
        }
        
        // Create entity
        ProductBrand productBrand = productBrandMapper.toEntity(request);
        productBrand.setProduct(product);
        productBrand.setBrand(brand);
        
        // Set production cost sheet if provided
        if (request.getProductionCostSheetId() != null) {
            ProductionCostSheet sheet = productionCostSheetRepository.findById(request.getProductionCostSheetId())
                .orElseThrow(() -> new RuntimeException("ProductionCostSheet not found: " + request.getProductionCostSheetId()));
            productBrand.setProductionCostSheet(sheet);
            // Auto-sync unitCost
            productBrand.updateProductionCostFromSheet();
        }
        
        // Save (selling price auto-calculated by @PrePersist)
        ProductBrand saved = productBrandRepository.save(productBrand);
        
        return productBrandMapper.toDto(saved);
    }

    @Override
    public ProductBrandDto update(Long id, UpsertProductBrandRequest request) {
        ProductBrand productBrand = productBrandRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ProductBrand not found: " + id));
        
        // Update fields using mapper
        productBrandMapper.updateEntityFromRequest(request, productBrand);
        
        // Update production cost sheet if changed
        if (request.getProductionCostSheetId() != null 
            && !request.getProductionCostSheetId().equals(
                productBrand.getProductionCostSheet() != null ? productBrand.getProductionCostSheet().getId() : null)) {
            ProductionCostSheet sheet = productionCostSheetRepository.findById(request.getProductionCostSheetId())
                .orElseThrow(() -> new RuntimeException("ProductionCostSheet not found: " + request.getProductionCostSheetId()));
            productBrand.setProductionCostSheet(sheet);
            productBrand.updateProductionCostFromSheet();
        }
        
        // Save (selling price auto-calculated by @PreUpdate)
        ProductBrand updated = productBrandRepository.save(productBrand);
        
        return productBrandMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBrandDto getById(Long id) {
        ProductBrand productBrand = productBrandRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ProductBrand not found: " + id));
        return productBrandMapper.toDto(productBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBrandDto> getByProductId(Long productId) {
        List<ProductBrand> list = productBrandRepository.findByProductId(productId);
        return list.stream().map(productBrandMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBrandDto> getByBrandId(Long brandId) {
        List<ProductBrand> list = productBrandRepository.findByBrandId(brandId);
        return list.stream().map(productBrandMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBrandDto getByProductIdAndBrandId(Long productId, Long brandId) {
        ProductBrand productBrand = productBrandRepository.findByProductIdAndBrandId(productId, brandId)
            .orElseThrow(() -> new RuntimeException("ProductBrand not found for product: " + productId + ", brand: " + brandId));
        return productBrandMapper.toDto(productBrand);
    }

    @Override
    public void delete(Long id) {
        if (!productBrandRepository.existsById(id)) {
            throw new RuntimeException("ProductBrand not found: " + id);
        }
        productBrandRepository.deleteById(id);
    }

    @Override
    public List<ProductBrandDto> deleteMultiple(List<Long> ids) {
        List<ProductBrand> toDelete = productBrandRepository.findAllById(ids);
        List<ProductBrandDto> deleted = toDelete.stream()
            .map(productBrandMapper::toDto)
            .collect(Collectors.toList());
        productBrandRepository.deleteAll(toDelete);
        return deleted;
    }

    @Override
    public ProductBrandDto toggleActive(Long id) {
        ProductBrand productBrand = productBrandRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ProductBrand not found: " + id));
        productBrand.setIsActive(!productBrand.getIsActive());
        ProductBrand updated = productBrandRepository.save(productBrand);
        return productBrandMapper.toDto(updated);
    }

    @Override
    public ProductBrandDto updateMaterialCost(Long productBrandId, BigDecimal materialCost) {
        ProductBrand productBrand = productBrandRepository.findById(productBrandId)
            .orElseThrow(() -> new RuntimeException("ProductBrand not found: " + productBrandId));
        
        productBrand.setMaterialCost(materialCost);
        // Selling price will be auto-recalculated by @PreUpdate
        
        ProductBrand updated = productBrandRepository.save(productBrand);
        return productBrandMapper.toDto(updated);
    }

    @Override
    public ProductBrandDto syncProductionCostFromSheet(Long productBrandId) {
        ProductBrand productBrand = productBrandRepository.findById(productBrandId)
            .orElseThrow(() -> new RuntimeException("ProductBrand not found: " + productBrandId));
        
        if (productBrand.getProductionCostSheet() == null) {
            throw new RuntimeException("ProductionCostSheet not set for this ProductBrand");
        }
        
        productBrand.updateProductionCostFromSheet();
        // Selling price will be auto-recalculated
        
        ProductBrand updated = productBrandRepository.save(productBrand);
        return productBrandMapper.toDto(updated);
    }

    @Override
    public void batchUpdateMaterialCostByProduct(Long productId, BigDecimal materialCost) {
        List<ProductBrand> list = productBrandRepository.findByProductId(productId);
        
        for (ProductBrand pb : list) {
            pb.setMaterialCost(materialCost);
            // Selling price auto-recalc
        }
        
        productBrandRepository.saveAll(list);
    }
}
