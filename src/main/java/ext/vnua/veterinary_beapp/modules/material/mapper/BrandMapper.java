package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.BrandDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.brand.CreateBrandRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.brand.UpdateBrandRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandDto toBrandDto(Brand brand);
    Brand toCreateBrand(CreateBrandRequest request);
    void updateBrandFromRequest(UpdateBrandRequest request, @MappingTarget Brand brand);
}