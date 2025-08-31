package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.SupplierDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.supplier.CreateSupplierRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.supplier.GetSupplierRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.supplier.UpdateSupplierRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.SupplierMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Supplier;
import ext.vnua.veterinary_beapp.modules.material.service.SupplierService;
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
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
    private final SupplierMapper supplierMapper;

    @GetMapping
    @ApiOperation(value = "Lấy tất cả nhà cung cấp")
    public ResponseEntity<?> getAllSuppliers(@Valid @ModelAttribute GetSupplierRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getStart(), request.getLimit());
        Page<Supplier> page = supplierService.getAllSupplier(request, pageRequest);

        return BaseResponse.successListData(page.getContent().stream()
                .map(supplierMapper::toSupplierDto)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Lấy tất cả nhà cung cấp theo id")
    public ResponseEntity<?> getSupplierById(@PathVariable Long id) {
        SupplierDto supplierDto = supplierService.selectSupplierById(id);
        return ResponseEntity.ok(supplierDto);
    }

    @GetMapping("/code/{supplierCode}")
    @ApiOperation(value = "Lấy thông tin nhà cung cấp theo mã")
    public ResponseEntity<?> getSupplierByCode(@PathVariable String supplierCode) {
        SupplierDto supplierDto = supplierService.selectSupplierByCode(supplierCode);
        return ResponseEntity.ok(supplierDto);
    }

    @GetMapping("/active")
    @ApiOperation(value = "Lấy danh sách nhà cung cấp đang hoạt động")
    public ResponseEntity<?> getActiveSuppliers() {
        List<SupplierDto> suppliers = supplierService.selectActiveSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/country/{countryOfOrigin}")
    @ApiOperation(value = "Lấy danh sách nhà cung cấp theo quốc gia")
    public ResponseEntity<?> getSuppliersByCountry(@PathVariable String countryOfOrigin) {
        List<SupplierDto> suppliers = supplierService.getSuppliersByCountry(countryOfOrigin);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/gmp/expiring/{days}")
    @ApiOperation(value = "Lấy danh sách nhà cung cấp có chứng chỉ GMP sắp hết hạn")
    public ResponseEntity<?> getSuppliersWithExpiringGmp(@PathVariable int days) {
        List<SupplierDto> suppliers = supplierService.getSuppliersWithExpiringGmp(days);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/gmp/expired")
    @ApiOperation(value = "Lấy danh sách nhà cung cấp có chứng chỉ GMP đã hết hạn")
    public ResponseEntity<?> getSuppliersWithExpiredGmp() {
        List<SupplierDto> suppliers = supplierService.getSuppliersWithExpiredGmp();
        return ResponseEntity.ok(suppliers);
    }

    @PostMapping
    @ApiOperation(value = "Tạo mới nhà cung cấp")
    public ResponseEntity<?> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        SupplierDto supplierDto = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierDto);
    }

    @PutMapping
    @ApiOperation(value = "Cập nhật thông tin nhà cung cấp")
    public ResponseEntity<?> updateSupplier(@Valid @RequestBody UpdateSupplierRequest request) {
        SupplierDto supplierDto = supplierService.updateSupplier(request);
        return ResponseEntity.ok(supplierDto);
    }

    @PutMapping("/{id}/toggle-status")
    @ApiOperation(value = "Chuyển đổi trạng thái hoạt động của nhà cung cấp")
    public ResponseEntity<?> toggleSupplierStatus(@PathVariable Long id) {
        supplierService.toggleActiveStatus(id);
        return ResponseEntity.ok("Chuyển đổi trạng thái nhà cung cấp thành công id " + id);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Xóa nhà cung cấp theo ID")
    public ResponseEntity<?> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok("Xóa nhà cung cấp thành công id "+ id);
    }

    @DeleteMapping("/bulk")
    @ApiOperation(value = "Xóa nhiều nhà cung cấp theo danh sách ID")
    public ResponseEntity<?> deleteSuppliers(@RequestBody List<Long> ids) {
        List<SupplierDto> deletedSuppliers = supplierService.deleteAllIdSuppliers(ids);
        return ResponseEntity.ok(deletedSuppliers);
    }
}
