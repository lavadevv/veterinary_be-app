package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.BrandDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.brand.CreateBrandRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.brand.GetBrandRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.brand.UpdateBrandRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.BrandMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Brand;
import ext.vnua.veterinary_beapp.modules.material.service.BrandService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;
    private final BrandMapper brandMapper;

    @GetMapping
    @ApiOperation(value = "Lấy tất cả thương hiệu")
    public ResponseEntity<?> getAllBrands(@Valid @ModelAttribute GetBrandRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getStart(), request.getLimit());
        Page<Brand> page = brandService.getAllBrand(request, pageRequest);

        return BaseResponse.successListData(page.getContent().stream()
                .map(brandMapper::toBrandDto)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @GetMapping("/all")
    @ApiOperation(value = "Lấy tất cả thương hiệu không phân trang")
    public ResponseEntity<?> getAllBrandsWithoutPaging() {
        List<BrandDto> brands = brandService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Lấy thông tin thương hiệu theo ID")
    public ResponseEntity<?> getBrandById(@PathVariable Long id) {
        BrandDto brand = brandService.selectBrandById(id);
        return ResponseEntity.ok(brand);
    }

    @PostMapping
    @ApiOperation(value = "Tạo mới thương hiệu")
    public ResponseEntity<?> createBrand(@Valid @RequestBody CreateBrandRequest request) {
        BrandDto createdBrand = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdBrand);
    }

    @PutMapping
    @ApiOperation(value = "Cập nhật thông tin thương hiệu")
    public ResponseEntity<?> updateBrand(@Valid @RequestBody UpdateBrandRequest request) {
        BrandDto updatedBrand = brandService.updateBrand(request);
        return ResponseEntity.ok(updatedBrand);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Xóa thương hiệu theo ID")
    public ResponseEntity<?> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok("Xóa thương hiệu thành công id " + id);
    }

    @DeleteMapping("/bulk")
    @ApiOperation(value = "Xóa nhiều thương hiệu theo danh sách ID")
    public ResponseEntity<?> deleteBrands(@RequestBody List<Long> ids) {
        List<BrandDto> deletedBrands = brandService.deleteAllIdBrands(ids);
        return ResponseEntity.ok(deletedBrands);
    }
}