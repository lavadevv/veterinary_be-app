// ext/vnua/veterinary_beapp/modules/material/controller/UnitOfMeasureController.java
package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.UnitOfMeasureDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.uom.CreateUnitOfMeasureRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.uom.GetUnitOfMeasureRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.uom.UpdateUnitOfMeasureRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.UnitOfMeasureMapper;
import ext.vnua.veterinary_beapp.modules.material.model.UnitOfMeasure;
import ext.vnua.veterinary_beapp.modules.material.repository.UnitOfMeasureRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomUnitOfMeasureQuery;
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
public class UnitOfMeasureController {

    private final UnitOfMeasureRepository uomRepo;
    private final UnitOfMeasureMapper uomMapper;

    @ApiOperation("Danh sách UnitOfMeasure (paging/sort/search name)")
    @GetMapping("/uoms")
    public ResponseEntity<?> list(@Valid @ModelAttribute GetUnitOfMeasureRequest req) {
        Page<UnitOfMeasure> page = uomRepo.findAll(
                CustomUnitOfMeasureQuery.getFilterUom(req),
                PageRequest.of(req.getStart(), req.getLimit())
        );
        var items = page.getContent().stream().map(uomMapper::toDto).toList();
        return BaseResponse.successListData(items, (int) page.getTotalElements());
    }

    @ApiOperation("Tạo UnitOfMeasure (ADMIN)")
    @PostMapping("/uoms")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody CreateUnitOfMeasureRequest req) {
        String name = req.getName().trim();
        if (uomRepo.existsByNameIgnoreCase(name)) {
            return ResponseEntity.badRequest().body("Tên đơn vị đã tồn tại");
        }
        UnitOfMeasure e = new UnitOfMeasure();
        e.setName(name);
        var saved = uomRepo.save(e);
        return ResponseEntity.ok(uomMapper.toDto(saved));
    }

    @ApiOperation("Cập nhật UnitOfMeasure (ADMIN)")
    @PutMapping("/uoms")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateUnitOfMeasureRequest req) {
        UnitOfMeasure e = uomRepo.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Đơn vị đo không tồn tại"));

        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            String name = req.getName().trim();
            if (!name.equalsIgnoreCase(e.getName()) && uomRepo.existsByNameIgnoreCase(name)) {
                return ResponseEntity.badRequest().body("Tên đơn vị đã tồn tại");
            }
            e.setName(name);
        }
        var saved = uomRepo.save(e);
        return ResponseEntity.ok(uomMapper.toDto(saved));
    }

    @ApiOperation("Xoá UnitOfMeasure (ADMIN)")
    @DeleteMapping("/uoms/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            uomRepo.deleteById(id);
            return ResponseEntity.ok("OK");
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest().body("Không thể xoá: đang được tham chiếu bởi materials");
        }
    }
}
