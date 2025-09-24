package ext.vnua.veterinary_beapp.modules.product.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;

import ext.vnua.veterinary_beapp.modules.product.dto.ProductDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.product.CreateProductRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.product.GetProductRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.product.UpdateProductRequest;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductMapper;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.servies.ProductService;
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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @ApiOperation(value = "Lấy tất cả sản phẩm với phân trang và bộ lọc")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER','PRODUCTION_MANAGER','QC_STAFF','OPERATOR','MAINTENANCE_STAFF','REGULATORY_AFFAIRS','SALES_STAFF','ACCOUNTANT','VIEWER')")
    public ResponseEntity<?> getAllProducts(@Valid @ModelAttribute GetProductRequest request) {
        Page<Product> page = productService.getAllProducts(request, PageRequest.of(request.getStart(), request.getLimit()));

        return BaseResponse.successListData(page.getContent().stream()
                .map(productMapper::toProductDto)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @ApiOperation(value = "Lấy thông tin chi tiết sản phẩm theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER','PRODUCTION_MANAGER','QC_STAFF','OPERATOR','MAINTENANCE_STAFF','REGULATORY_AFFAIRS','SALES_STAFF','ACCOUNTANT','VIEWER')")
    public ResponseEntity<?> getProductById(@PathVariable("id") Long id) {
        ProductDto product = productService.selectProductById(id);
        return ResponseEntity.ok(product);
    }

    @ApiOperation(value = "Lấy thông tin chi tiết sản phẩm theo mã")
    @GetMapping("/code/{productCode}")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER','PRODUCTION_MANAGER','QC_STAFF','OPERATOR','MAINTENANCE_STAFF','REGULATORY_AFFAIRS','SALES_STAFF','ACCOUNTANT','VIEWER')")
    public ResponseEntity<?> getProductByCode(
            @ApiParam(value = "Mã sản phẩm", required = true)
            @PathVariable("productCode") String productCode) {
        ProductDto product = productService.selectProductByCode(productCode);
        return ResponseEntity.ok(product);
    }

    @ApiOperation(value = "Lấy danh sách sản phẩm theo danh mục")
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER','PRODUCTION_MANAGER','QC_STAFF','SALES_STAFF','ACCOUNTANT','VIEWER')")
    public ResponseEntity<?> getProductsByCategory(
            @ApiParam(value = "Danh mục sản phẩm", required = true)
            @PathVariable("category") String category) {
        List<ProductDto> products = productService.getProductsByCategory(category);
        return BaseResponse.successListData(products, products.size());
    }

    @ApiOperation(value = "Tạo sản phẩm mới")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','REGULATORY_AFFAIRS','WAREHOUSE_MANAGER')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductDto newProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    @ApiOperation(value = "Cập nhật thông tin sản phẩm")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','REGULATORY_AFFAIRS','WAREHOUSE_MANAGER')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        request.setId(id);
        ProductDto updatedProduct = productService.updateProduct(request);
        return ResponseEntity.ok(updatedProduct);
    }

    @ApiOperation(value = "Cập nhật tồn kho hiện tại của sản phẩm")
    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER','SALES_STAFF','ACCOUNTANT')")
    public ResponseEntity<?> updateStock(
            @ApiParam(value = "ID sản phẩm", required = true)
            @PathVariable("id") Long id,
            @ApiParam(value = "Số lượng tồn kho mới", required = true)
            @RequestParam("stock") Double stock) {
        productService.updateStock(id, stock);
        return ResponseEntity.ok("Cập nhật tồn kho thành công");
    }

    @ApiOperation(value = "Chuyển đổi trạng thái hoạt động của sản phẩm")
    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','REGULATORY_AFFAIRS')")
    public ResponseEntity<?> toggleActiveStatus(
            @ApiParam(value = "ID sản phẩm", required = true)
            @PathVariable("id") Long id) {
        productService.toggleActiveStatus(id);
        return ResponseEntity.ok("Chuyển đổi trạng thái thành công");
    }

    @ApiOperation(value = "Xóa một sản phẩm")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteProduct(
            @ApiParam(value = "ID sản phẩm", required = true)
            @PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Xóa sản phẩm thành công");
    }

    @ApiOperation(value = "Xóa nhiều sản phẩm theo danh sách ID")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteMultipleProducts(
            @ApiParam(value = "Danh sách ID sản phẩm cần xóa", required = true)
            @RequestBody List<Long> ids) {
        List<ProductDto> deletedProducts = productService.deleteAllProducts(ids);
        return BaseResponse.successListData(deletedProducts, deletedProducts.size());
    }
}
