package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.BrandDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.brand.CreateBrandRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.brand.UpdateBrandRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.BrandMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Brand;
import ext.vnua.veterinary_beapp.modules.material.repository.BrandRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomBrandQuery;
import ext.vnua.veterinary_beapp.modules.material.service.BrandService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    public Page<Brand> getAllBrand(CustomBrandQuery.BrandFilterParam param, PageRequest pageRequest) {
        Specification<Brand> specification = CustomBrandQuery.getFilterBrand(param);
        return brandRepository.findAll(specification, pageRequest);
    }

    @Override
    public BrandDto selectBrandById(Long id) {
        Optional<Brand> brandOptional = brandRepository.findById(id);
        if (brandOptional.isEmpty()) {
            throw new DataExistException("Thương hiệu không tồn tại");
        }
        Brand brand = brandOptional.get();
        return brandMapper.toBrandDto(brand);
    }

    @Override
    public BrandDto selectBrandByName(String name) {
        Optional<Brand> brandOptional = brandRepository.findByName(name);
        if (brandOptional.isEmpty()) {
            throw new DataExistException("Tên thương hiệu không tồn tại");
        }
        Brand brand = brandOptional.get();
        return brandMapper.toBrandDto(brand);
    }

    @Override
    public List<BrandDto> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        return brands.stream()
                .map(brandMapper::toBrandDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "Brand", description = "Tạo mới thương hiệu")
    public BrandDto createBrand(CreateBrandRequest request) {
        // Validate brand name is unique
        Optional<Brand> existingBrand = brandRepository.findByName(request.getName());
        if (existingBrand.isPresent()) {
            throw new DataExistException("Tên thương hiệu đã tồn tại");
        }

        try {
            Brand brand = brandMapper.toCreateBrand(request);
            return brandMapper.toBrandDto(brandRepository.saveAndFlush(brand));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm thương hiệu");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "Brand", description = "Cập nhật thương hiệu")
    public BrandDto updateBrand(UpdateBrandRequest request) {
        Optional<Brand> brandOptional = brandRepository.findById(request.getId());
        if (brandOptional.isEmpty()) {
            throw new DataExistException("Thương hiệu không tồn tại");
        }

        Brand existingBrand = brandOptional.get();

        // Validate brand name is unique (excluding current brand)
        if (!existingBrand.getName().equals(request.getName())) {
            Optional<Brand> duplicateBrand = brandRepository
                    .findByNameAndIdNot(request.getName(), request.getId());
            if (duplicateBrand.isPresent()) {
                throw new DataExistException("Tên thương hiệu đã tồn tại");
            }
        }

        try {
            brandMapper.updateBrandFromRequest(request, existingBrand);
            return brandMapper.toBrandDto(brandRepository.saveAndFlush(existingBrand));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật thương hiệu");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Brand", description = "Xóa thương hiệu")
    public void deleteBrand(Long id) {
        Optional<Brand> brandOptional = brandRepository.findById(id);
        if (brandOptional.isEmpty()) {
            throw new DataExistException("Thương hiệu không tồn tại");
        }

        try {
            brandRepository.deleteById(id);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa thương hiệu");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Brand", description = "Xóa danh sách thương hiệu")
    public List<BrandDto> deleteAllIdBrands(List<Long> ids) {
        List<BrandDto> brandDtos = new ArrayList<>();
        for (Long id : ids) {
            Optional<Brand> optionalBrand = brandRepository.findById(id);
            if (optionalBrand.isPresent()) {
                Brand brand = optionalBrand.get();
                brandDtos.add(brandMapper.toBrandDto(brand));
                brandRepository.delete(brand);
            } else {
                throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách thương hiệu!");
            }
        }
        return brandDtos;
    }

}