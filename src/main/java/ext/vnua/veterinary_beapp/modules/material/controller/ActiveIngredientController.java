package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.ActiveIngredientDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.CreateActiveIngredientRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.GetActiveIngredientRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.UpdateActiveIngredientRequest;
import ext.vnua.veterinary_beapp.modules.material.model.ActiveIngredient;
import ext.vnua.veterinary_beapp.modules.material.service.ActiveIngredientService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material/master")
@RequiredArgsConstructor
public class ActiveIngredientController {

    private final ActiveIngredientService activeIngredientService;

    @GetMapping("/active-ingredients")
    @ApiOperation(value = "Lấy danh sách hoạt chất (phân trang)")
    public ResponseEntity<?> getAllActiveIngredients(@Valid @ModelAttribute GetActiveIngredientRequest request) {
        Page<ActiveIngredient> page = activeIngredientService.search(
                request,
                PageRequest.of(request.getStart(), request.getLimit())
        );

        return BaseResponse.successListData(
                page.getContent(),
                (int) page.getTotalElements()
        );
    }

    @GetMapping("/active-ingredients/active")
    @ApiOperation(value = "Lấy danh sách hoạt chất đang hoạt động")
    public ResponseEntity<?> getActiveIngredients() {
        List<ActiveIngredientDto> activeIngredients = activeIngredientService.getActiveIngredients();
        return ResponseEntity.ok(activeIngredients);
    }

    @GetMapping("/active-ingredients/{id}")
    @ApiOperation(value = "Lấy hoạt chất theo ID")
    public ResponseEntity<?> getActiveIngredientById(@PathVariable Long id) {
        ActiveIngredientDto activeIngredient = activeIngredientService.getActiveIngredientById(id);
        return ResponseEntity.ok(activeIngredient);
    }

    @GetMapping("/active-ingredients/code/{code}")
    @ApiOperation(value = "Lấy hoạt chất theo mã")
    public ResponseEntity<?> getActiveIngredientByCode(@PathVariable String code) {
        ActiveIngredientDto activeIngredient = activeIngredientService.getActiveIngredientByCode(code);
        return ResponseEntity.ok(activeIngredient);
    }

    @GetMapping("/active-ingredients/search")
    @ApiOperation(value = "Tìm kiếm hoạt chất theo từ khóa")
    public ResponseEntity<?> searchActiveIngredients(@RequestParam String keyword) {
        List<ActiveIngredientDto> activeIngredients = activeIngredientService.searchByKeyword(keyword);
        return ResponseEntity.ok(activeIngredients);
    }

    @PostMapping("/active-ingredients")
    @ApiOperation(value = "Tạo mới hoạt chất (ADMIN)")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> createActiveIngredient(@Valid @RequestBody CreateActiveIngredientRequest request) {
        ActiveIngredientDto created = activeIngredientService.createActiveIngredient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/active-ingredients/{id}")
    @ApiOperation(value = "Cập nhật hoạt chất (ADMIN)")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> updateActiveIngredient(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateActiveIngredientRequest request) {
        request.setId(id);
        ActiveIngredientDto updated = activeIngredientService.updateActiveIngredient(request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/active-ingredients/{id}")
    @ApiOperation(value = "Xóa hoạt chất (ADMIN)")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteActiveIngredient(@PathVariable Long id) {
        activeIngredientService.deleteActiveIngredient(id);
        return ResponseEntity.ok("Xóa hoạt chất thành công");
    }

    @PatchMapping("/active-ingredients/{id}/toggle-status")
    @ApiOperation(value = "Bật/tắt trạng thái hoạt chất (ADMIN)")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> toggleActiveIngredientStatus(@PathVariable Long id) {
        activeIngredientService.toggleActiveStatus(id);
        return ResponseEntity.ok("Đã thay đổi trạng thái hoạt chất");
    }
}