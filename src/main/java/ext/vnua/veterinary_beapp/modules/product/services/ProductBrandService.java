package ext.vnua.veterinary_beapp.modules.product.services;

import ext.vnua.veterinary_beapp.modules.product.dto.response.ProductBrandDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productbrand.UpsertProductBrandRequest;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBrand;
import ext.vnua.veterinary_beapp.modules.product.repository.custom.CustomProductBrandQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface cho ProductBrand
 */
public interface ProductBrandService {
    
    /**
     * Lấy danh sách ProductBrand với filter và phân trang
     */
    Page<ProductBrand> getAllProductBrands(CustomProductBrandQuery.ProductBrandFilterParam param, PageRequest pageRequest);
    
    /**
     * Tạo mới ProductBrand
     */
    ProductBrandDto create(UpsertProductBrandRequest request);
    
    /**
     * Cập nhật ProductBrand
     */
    ProductBrandDto update(Long id, UpsertProductBrandRequest request);
    
    /**
     * Lấy chi tiết ProductBrand theo ID
     */
    ProductBrandDto getById(Long id);
    
    /**
     * Lấy danh sách ProductBrand theo Product
     */
    List<ProductBrandDto> getByProductId(Long productId);
    
    /**
     * Lấy danh sách ProductBrand theo Brand
     */
    List<ProductBrandDto> getByBrandId(Long brandId);
    
    /**
     * Lấy ProductBrand theo Product và Brand
     */
    ProductBrandDto getByProductIdAndBrandId(Long productId, Long brandId);
    
    /**
     * Xóa ProductBrand
     */
    void delete(Long id);
    
    /**
     * Xóa nhiều ProductBrand
     */
    List<ProductBrandDto> deleteMultiple(List<Long> ids);
    
    /**
     * Toggle active status
     */
    ProductBrandDto toggleActive(Long id);
    
    /**
     * Cập nhật material cost từ Formula calculation
     */
    ProductBrandDto updateMaterialCost(Long productBrandId, BigDecimal materialCost);
    
    /**
     * Cập nhật production cost từ ProductionCostSheet
     */
    ProductBrandDto syncProductionCostFromSheet(Long productBrandId);
    
    /**
     * Batch update material cost cho tất cả ProductBrand của 1 Product
     */
    void batchUpdateMaterialCostByProduct(Long productId, BigDecimal materialCost);
}
