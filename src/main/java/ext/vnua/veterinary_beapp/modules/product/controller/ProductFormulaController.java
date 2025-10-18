package ext.vnua.veterinary_beapp.modules.product.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductFormulaDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.formula.GetProductFormulaRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.formula.ProductFormulaListRow;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.UpsertFormulaRequest;
import ext.vnua.veterinary_beapp.modules.product.services.ProductFormulaService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    // ======= NEW: danh sách toàn cục với filter + paging =======
    @ApiOperation(value = "Lấy danh sách công thức (toàn cục) – filter + paging")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF','VIEWER')")
    public ResponseEntity<?> listAll(@Valid @ModelAttribute GetProductFormulaRequest req) {
        Page<ProductFormulaListRow> page =
                formulaService.getAllFormulaRows(req, PageRequest.of(req.getStart(), req.getLimit()));

        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }

    // ======= NEW: xem chi tiết 1 công thức theo ID (phục vụ click từ list toàn cục) =======
    @ApiOperation(value = "Xem chi tiết công thức theo ID (cho màn danh sách toàn cục)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF','VIEWER')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(formulaService.getById(id));
    }
}
