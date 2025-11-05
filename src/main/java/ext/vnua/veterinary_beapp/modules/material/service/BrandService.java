package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.BrandDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.brand.CreateBrandRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.brand.UpdateBrandRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Brand;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomBrandQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface BrandService {
    Page<Brand> getAllBrand(CustomBrandQuery.BrandFilterParam param, PageRequest pageRequest);
    
    List<BrandDto> getAllBrands();
    
    BrandDto selectBrandById(Long id);
    
    BrandDto selectBrandByName(String name);
    
    BrandDto createBrand(CreateBrandRequest request);
    
    BrandDto updateBrand(UpdateBrandRequest request);
    
    void deleteBrand(Long id);
    
    List<BrandDto> deleteAllIdBrands(List<Long> ids);
}