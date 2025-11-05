package ext.vnua.veterinary_beapp.modules.product.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.product.dto.response.ProductBrandDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productbrand.GetProductBrandRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productbrand.UpsertProductBrandRequest;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductBrandMapper;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBrand;
import ext.vnua.veterinary_beapp.modules.product.services.ProductBrandService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller cho ProductBrand
 * Quản lý thông tin sản phẩm theo brand
 */
@RestController
@RequestMapping("/product-brands")
@RequiredArgsConstructor
@Api(tags = "Product Brand Management")
public class ProductBrandController {

    private final ProductBrandService productBrandService;
    private final ProductBrandMapper productBrandMapper;

    @ApiOperation(value = "Lấy danh sách ProductBrand với phân trang và bộ lọc")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER','PRODUCTION_MANAGER','QC_STAFF','OPERATOR','MAINTENANCE_STAFF','REGULATORY_AFFAIRS','SALES_STAFF','ACCOUNTANT','VIEWER')")
    public ResponseEntity<?> getAllProductBrands(@Valid @ModelAttribute GetProductBrandRequest request) {
        Page<ProductBrand> page = productBrandService.getAllProductBrands(request, PageRequest.of(request.getStart(), request.getLimit()));

        return BaseResponse.successListData(page.getContent().stream()
                .map(productBrandMapper::toDto)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @ApiOperation(value = "Lấy chi tiết ProductBrand theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER','PRODUCTION_MANAGER','QC_STAFF','SALES_STAFF','ACCOUNTANT','VIEWER')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        ProductBrandDto productBrand = productBrandService.getById(id);
        return ResponseEntity.ok(productBrand);
    }

    @ApiOperation(value = "Lấy danh sách ProductBrand theo Product")
    @GetMapping("/by-product/{productId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER','PRODUCTION_MANAGER','QC_STAFF','SALES_STAFF','ACCOUNTANT','VIEWER')")
    public ResponseEntity<?> getByProductId(@PathVariable Long productId) {
        List<ProductBrandDto> list = productBrandService.getByProductId(productId);
        return BaseResponse.successListData(list, list.size());
    }

    @ApiOperation(value = "Lấy danh sách ProductBrand theo Brand")
    @GetMapping("/by-brand/{brandId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER','PRODUCTION_MANAGER','QC_STAFF','SALES_STAFF','ACCOUNTANT','VIEWER')")
    public ResponseEntity<?> getByBrandId(@PathVariable Long brandId) {
        List<ProductBrandDto> list = productBrandService.getByBrandId(brandId);
        return BaseResponse.successListData(list, list.size());
    }

    @ApiOperation(value = "Lấy ProductBrand theo Product và Brand")
    @GetMapping("/by-product-brand")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER','PRODUCTION_MANAGER','QC_STAFF','SALES_STAFF','ACCOUNTANT','VIEWER')")
    public ResponseEntity<?> getByProductIdAndBrandId(
            @ApiParam(value = "Product ID", required = true) @RequestParam Long productId,
            @ApiParam(value = "Brand ID", required = true) @RequestParam Long brandId) {
        ProductBrandDto productBrand = productBrandService.getByProductIdAndBrandId(productId, brandId);
        return ResponseEntity.ok(productBrand);
    }

    @ApiOperation(value = "Tạo mới ProductBrand")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','REGULATORY_AFFAIRS','WAREHOUSE_MANAGER')")
    public ResponseEntity<?> create(@Valid @RequestBody UpsertProductBrandRequest request) {
        ProductBrandDto newProductBrand = productBrandService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProductBrand);
    }

    @ApiOperation(value = "Cập nhật ProductBrand")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','REGULATORY_AFFAIRS','WAREHOUSE_MANAGER')")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody UpsertProductBrandRequest request) {
        ProductBrandDto updatedProductBrand = productBrandService.update(id, request);
        return ResponseEntity.ok(updatedProductBrand);
    }

    @ApiOperation(value = "Xóa ProductBrand")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> delete(
            @ApiParam(value = "ID ProductBrand", required = true) @PathVariable Long id) {
        productBrandService.delete(id);
        return ResponseEntity.ok("Xóa ProductBrand thành công");
    }

    @ApiOperation(value = "Xóa nhiều ProductBrand theo danh sách ID")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteMultiple(
            @ApiParam(value = "Danh sách ID ProductBrand cần xóa", required = true) @RequestBody List<Long> ids) {
        List<ProductBrandDto> deletedProductBrands = productBrandService.deleteMultiple(ids);
        return BaseResponse.successListData(deletedProductBrands, deletedProductBrands.size());
    }

    @ApiOperation(value = "Chuyển đổi trạng thái hoạt động")
    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','REGULATORY_AFFAIRS')")
    public ResponseEntity<?> toggleActive(
            @ApiParam(value = "ID ProductBrand", required = true) @PathVariable Long id) {
        ProductBrandDto productBrand = productBrandService.toggleActive(id);
        return ResponseEntity.ok(productBrand);
    }

    @ApiOperation(value = "Cập nhật chi phí nguyên liệu (từ Formula calculation)")
    @PatchMapping("/{id}/update-material-cost")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','ACCOUNTANT')")
    public ResponseEntity<?> updateMaterialCost(
            @ApiParam(value = "ID ProductBrand", required = true) @PathVariable Long id,
            @ApiParam(value = "Chi phí nguyên liệu mới", required = true) @RequestParam BigDecimal materialCost) {
        ProductBrandDto productBrand = productBrandService.updateMaterialCost(id, materialCost);
        return ResponseEntity.ok(productBrand);
    }

    @ApiOperation(value = "Đồng bộ chi phí sản xuất từ ProductionCostSheet")
    @PatchMapping("/{id}/sync-production-cost")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','ACCOUNTANT')")
    public ResponseEntity<?> syncProductionCost(
            @ApiParam(value = "ID ProductBrand", required = true) @PathVariable Long id) {
        ProductBrandDto productBrand = productBrandService.syncProductionCostFromSheet(id);
        return ResponseEntity.ok(productBrand);
    }

    @ApiOperation(value = "Cập nhật hàng loạt chi phí nguyên liệu cho tất cả brand của 1 product")
    @PatchMapping("/batch-update-material-cost")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','ACCOUNTANT')")
    public ResponseEntity<?> batchUpdateMaterialCost(
            @ApiParam(value = "Product ID", required = true) @RequestParam Long productId,
            @ApiParam(value = "Chi phí nguyên liệu mới", required = true) @RequestParam BigDecimal materialCost) {
        productBrandService.batchUpdateMaterialCostByProduct(productId, materialCost);
        return ResponseEntity.ok("Cập nhật chi phí nguyên liệu thành công");
    }
}
