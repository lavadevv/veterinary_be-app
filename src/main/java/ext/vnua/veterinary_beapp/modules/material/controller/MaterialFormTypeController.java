// File: ext/vnua/veterinary_beapp/modules/material/controller/MaterialFormTypeController.java
package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialFormType.CreateMaterialFormTypeRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialFormType.GetMaterialFormTypeRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialFormType.UpdateMaterialFormTypeRequest;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialFormType;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialFormTypeRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialFormTypeQuery;
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
public class MaterialFormTypeController {

    private final MaterialFormTypeRepository repo;

    @ApiOperation("Danh sách dạng vật liệu (paging/sort/search)")
    @GetMapping("/material-form-types")
    public ResponseEntity<?> list(@Valid @ModelAttribute GetMaterialFormTypeRequest req) {
        Page<MaterialFormType> page = repo.findAll(
                CustomMaterialFormTypeQuery.getFilter(req),
                PageRequest.of(req.getStart(), req.getLimit())
        );
        // Trả về entity trực tiếp
        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }

    @ApiOperation("Danh sách tất cả dạng vật liệu (A→Z)")
    @GetMapping("/material-form-types/all")
    public ResponseEntity<?> listAll() {
        var rows = repo.findAllByOrderByNameAsc();
        return BaseResponse.successListData(rows, rows.size());
    }

    @ApiOperation("Tạo dạng vật liệu (ADMIN)")
    @PostMapping("/material-form-types")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody CreateMaterialFormTypeRequest req) {
        repo.findByName(req.getName().trim())
                .ifPresent(x -> { throw new IllegalArgumentException("Tên đã tồn tại"); });

        MaterialFormType e = new MaterialFormType();
        e.setName(req.getName().trim());
        return BaseResponse.successData(repo.save(e));
    }

    @ApiOperation("Cập nhật dạng vật liệu (ADMIN)")
    @PutMapping("/material-form-types")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateMaterialFormTypeRequest req) {
        MaterialFormType e = repo.findById(req.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tồn tại"));

        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            if (repo.existsByNameAndIdNot(req.getName().trim(), req.getId())) {
                throw new IllegalArgumentException("Tên đã tồn tại");
            }
            e.setName(req.getName().trim());
        }
        return BaseResponse.successData(repo.save(e));
    }

    @ApiOperation("Xoá dạng vật liệu (ADMIN)")
    @DeleteMapping("/material-form-types/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            repo.deleteById(id);
            return BaseResponse.successData("OK");
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest().body("Không thể xoá: đang được tham chiếu");
        }
    }
}
