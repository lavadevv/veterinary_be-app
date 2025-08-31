package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.CreateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.GetMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.UpdateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.MaterialMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialQuery;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Tag;
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
@RequestMapping("/material")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final MaterialMapper materialMapper;

    @GetMapping
    @ApiOperation(value = "Lấy tất cả vật liệu")
    public ResponseEntity<?> getAllMaterials(@Valid @ModelAttribute GetMaterialRequest request) {
        Page<Material> page = materialService.getAllMaterial(request,
                PageRequest.of(request.getStart(), request.getLimit()));

        return BaseResponse.successListData(page.getContent().stream()
                .map(materialMapper::toMaterialDto)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Lấy material theo id")
    public ResponseEntity<?> getMaterialById(@PathVariable Long id) {
        MaterialDto materialDto = materialService.selectMaterialById(id);
        return ResponseEntity.ok(materialDto);
    }

    @GetMapping("/code/{materialCode}")
    @ApiOperation(value = "Lấy vật liệu theo code")
    public ResponseEntity<?> getMaterialByCode(@PathVariable String materialCode) {
        MaterialDto materialDto = materialService.selectMaterialByCode(materialCode);
        return ResponseEntity.ok(materialDto);
    }

    @GetMapping("/active")
    @ApiOperation(value = "Lấy tất cả materials đang hoạt động")
    public ResponseEntity<?> getAllActiveMaterials() {
        List<MaterialDto> materialDtos = materialService.getActiveMaterials();
        return ResponseEntity.ok(materialDtos);
    }

    @GetMapping("/supplier/{supplierId}")
    @ApiOperation(value = "Lấy materials theo nhà cung cấp")
    public ResponseEntity<?> getMaterialsBySupplier(@PathVariable Long supplierId) {
        List<MaterialDto> materialDtos = materialService.selectMaterialsBySupplier(supplierId);
        return ResponseEntity.ok(materialDtos);
    }

    @GetMapping("/low-stock")
    @ApiOperation(value = "Lấy materials có tồn kho thấp")
    public ResponseEntity<?> getLowStockMaterials() {
        List<MaterialDto> materialDtos = materialService.getLowStockMaterials();
        return ResponseEntity.ok(materialDtos);
    }

    @GetMapping("/cold-storage")
    @ApiOperation(value = "Lấy materials yêu cầu bảo quản lạnh")
    public ResponseEntity<?> getMaterialsRequiringColdStorage() {
        List<MaterialDto> materialDtos = materialService.getMaterialsRequiringColdStorage();
        return ResponseEntity.ok(materialDtos);
    }

    @PostMapping
    @ApiOperation(value = "Tạo mới material")
    public ResponseEntity<?> createMaterial(
            @Valid @RequestBody CreateMaterialRequest request) {
        MaterialDto materialDto = materialService.createMaterial(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(materialDto);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Cập nhật material")
    public ResponseEntity<?> updateMaterial(@PathVariable Long id,
                                            @Valid @RequestBody UpdateMaterialRequest request) {
        request.setId(id);
        MaterialDto materialDto = materialService.updateMaterial(request);
        return ResponseEntity.ok(materialDto);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Xoá material")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.ok("Xóa vật liệu thành công");
    }

    @DeleteMapping("/batch")
    @ApiOperation(value = "Xoá nhiều materials")
    public ResponseEntity<?> deleteMaterials(
            @RequestBody List<Long> ids) {
        List<MaterialDto> deletedMaterials = materialService.deleteAllIdMaterials(ids);
        return ResponseEntity.ok(deletedMaterials);
    }

    @PatchMapping("/{id}/toggle-status")
    @ApiOperation(value = "Toggle material active status")
    public ResponseEntity<?> toggleMaterialStatus(@PathVariable Long id) {
        materialService.toggleActiveStatus(id);
        return ResponseEntity.ok("Đã thay đổi trạng thái vật liệu");
    }

    @PatchMapping("/{id}/update-stock")
    @ApiOperation(value = "Cập nhật số lượng tồn kho")
    public ResponseEntity<?> updateStock(@PathVariable Long id,
                                         @RequestParam Double newStock) {
        materialService.updateCurrentStock(id, newStock);
        return ResponseEntity.ok("Cập nhật tồn kho thành công");
    }
}