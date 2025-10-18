package ext.vnua.veterinary_beapp.modules.product.controller;


import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductRegistrationDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration.CreateProductRegistrationRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration.GetProductRegistrationRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration.UpdateProductRegistrationRequest;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductRegistrationMapper;
import ext.vnua.veterinary_beapp.modules.product.model.ProductRegistration;
import ext.vnua.veterinary_beapp.modules.product.services.ProductRegistrationService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-registration")
@RequiredArgsConstructor
public class ProductRegistrationController {
    private final ProductRegistrationService regService;
    private final ProductRegistrationMapper mapper;

    @ApiOperation(value = "Filter đăng ký sản phẩm (paging)")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN','REGULATORY_AFFAIRS','VIEWER')")
    public ResponseEntity<?> list(@Valid @ModelAttribute GetProductRegistrationRequest req) {
        Page<ProductRegistration> page = regService.filter(req, PageRequest.of(req.getStart(), req.getLimit()));
        return BaseResponse.successListData(page.map(mapper::toDto).getContent(), (int) page.getTotalElements());
    }

    @ApiOperation(value = "Xem hồ sơ đăng ký theo sản phẩm")
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','REGULATORY_AFFAIRS','VIEWER')")
    public ResponseEntity<?> getByProduct(@PathVariable Long productId) {
        ProductRegistrationDto dto = regService.getByProduct(productId);
        return ResponseEntity.ok(dto);
    }

    @ApiOperation(value = "Tạo hồ sơ đăng ký")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN','REGULATORY_AFFAIRS')")
    public ResponseEntity<?> create(@Valid @RequestBody CreateProductRegistrationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(regService.create(req));
    }

    @ApiOperation(value = "Cập nhật hồ sơ đăng ký")
    @PutMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN','REGULATORY_AFFAIRS')")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateProductRegistrationRequest req) {
        return ResponseEntity.ok(regService.update(req));
    }

    @ApiOperation(value = "Xóa hồ sơ đăng ký")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','REGULATORY_AFFAIRS')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        regService.delete(id);
        return ResponseEntity.ok("Đã xóa đăng ký");
    }
}
