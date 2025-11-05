package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouseType.CreateWarehouseTypeRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouseType.GetWarehouseTypeRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouseType.UpdateWarehouseTypeRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.WarehouseMapper;
import ext.vnua.veterinary_beapp.modules.material.mapper.WarehouseTypeMapper;
import ext.vnua.veterinary_beapp.modules.material.model.WarehouseType;
import ext.vnua.veterinary_beapp.modules.material.repository.WarehouseTypeRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomWarehouseTypeQuery;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/material/master")
@RequiredArgsConstructor
public class WarehouseTypeController {


    private final WarehouseTypeRepository warehouseTypeRepository;
    private final WarehouseTypeMapper warehouseMapper;

    @ApiOperation("Danh sách loại kho (paging/sort/search name)")
    @GetMapping("/warehouse-types")
    public ResponseEntity<?> listWarehouseTypes(@Valid @ModelAttribute GetWarehouseTypeRequest req) {
        Page<WarehouseType> page = warehouseTypeRepository.findAll(
                CustomWarehouseTypeQuery.getFilterWarehouseType(req),
                PageRequest.of(req.getStart(), req.getLimit())
        );
        var items = page.getContent().stream().map(warehouseMapper::toWarehouseTypeDto).toList();
        return BaseResponse.successListData(items, (int) page.getTotalElements());
    }

    @ApiOperation("Tạo loại kho (ADMIN)")
    @PostMapping("/warehouse-types")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> createWarehouseType(@Valid @RequestBody CreateWarehouseTypeRequest req) {
        WarehouseType wt = new WarehouseType();
        wt.setName(req.getName().trim());
        WarehouseType saved = warehouseTypeRepository.save(wt);
        return ResponseEntity.ok(warehouseMapper.toWarehouseTypeDto(saved));
    }

    @ApiOperation("Cập nhật loại kho (ADMIN)")
    @PutMapping("/warehouse-types")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> updateWarehouseType(@Valid @RequestBody UpdateWarehouseTypeRequest req) {
        WarehouseType wt = warehouseTypeRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("WarehouseType không tồn tại"));

        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            wt.setName(req.getName().trim());
        }
        WarehouseType saved = warehouseTypeRepository.save(wt);
        return ResponseEntity.ok(warehouseMapper.toWarehouseTypeDto(saved));
    }

    @ApiOperation("Xoá loại kho (ADMIN)")
    @DeleteMapping("/warehouse-types/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteWarehouseType(@PathVariable Long id) {
        try {
            warehouseTypeRepository.deleteById(id);
            return ResponseEntity.ok("OK");
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest().body("Không thể xoá: đang được tham chiếu bởi warehouses");
        }
    }
}