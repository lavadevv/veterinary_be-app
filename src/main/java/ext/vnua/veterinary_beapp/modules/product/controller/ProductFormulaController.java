package ext.vnua.veterinary_beapp.modules.product.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductFormulaDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.UpsertFormulaRequest;
import ext.vnua.veterinary_beapp.modules.product.servies.ProductFormulaService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-formula")
@RequiredArgsConstructor
public class ProductFormulaController {
    private final ProductFormulaService formulaService;

    @ApiOperation(value = "Tạo/cập nhật công thức (theo version)")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF')")
    public ResponseEntity<?> upsert(@Valid @RequestBody UpsertFormulaRequest req) {
        ProductFormulaDto dto = formulaService.upsertFormula(req);
        return ResponseEntity.ok(dto);
    }

    @ApiOperation(value = "Kích hoạt công thức (độc nhất) cho sản phẩm")
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER')")
    public ResponseEntity<?> activate(@ApiParam(value = "ID công thức", required = true) @PathVariable("id") Long id) {
        formulaService.activateFormula(id);
        return ResponseEntity.ok("Đã kích hoạt công thức");
    }

    @ApiOperation(value = "Xem công thức active của sản phẩm")
    @GetMapping("/product/{productId}/active")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF','VIEWER')")
    public ResponseEntity<?> getActive(@PathVariable Long productId) {
        return ResponseEntity.ok(formulaService.getActiveFormula(productId));
    }

    @ApiOperation(value = "Danh sách công thức theo sản phẩm")
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF','VIEWER')")
    public ResponseEntity<?> list(@PathVariable Long productId) {
        List<ProductFormulaDto> list = formulaService.listFormulas(productId);
        return BaseResponse.successListData(list, list.size());
    }

    @ApiOperation(value = "Xóa công thức")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        formulaService.deleteFormula(id);
        return ResponseEntity.ok("Đã xóa công thức");
    }
}
