// File: ext/vnua/veterinary_beapp/modules/material/controller/MaterialCategoryController.java
package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialCategory.CreateMaterialCategoryRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialCategory.GetMaterialCategoryRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialCategory.UpdateMaterialCategoryRequest;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialCategory;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialCategoryQuery;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialCategoryService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material/master")
@RequiredArgsConstructor
public class MaterialCategoryController {

    private final MaterialCategoryService service;

    @ApiOperation("Danh sách loại vật liệu (paging/sort/search)")
    @GetMapping("/material-categories")
    public ResponseEntity<?> list(@Valid @ModelAttribute GetMaterialCategoryRequest req) {
        Page<MaterialCategory> page = service.search(req, PageRequest.of(req.getStart(), req.getLimit()));
        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }

    @ApiOperation("Danh sách tất cả loại vật liệu (A→Z)")
    @GetMapping("/material-categories/all")
    public ResponseEntity<?> listAll() {
        List<MaterialCategory> rows = service.listAll();
        return BaseResponse.successListData(rows, rows.size());
    }

    @ApiOperation("Tạo loại vật liệu (ADMIN)")
    @PostMapping("/material-categories")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody CreateMaterialCategoryRequest request) {
        MaterialCategory saved = service.create(request.getCategoryName());
        return BaseResponse.successData(saved);
    }

    @ApiOperation("Cập nhật loại vật liệu (ADMIN)")
    @PutMapping("/material-categories/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody UpdateMaterialCategoryRequest request) {
        MaterialCategory saved = service.update(id, request.getCategoryName());
        return BaseResponse.successData(saved);
    }

    @ApiOperation("Xoá loại vật liệu (ADMIN)")
    @DeleteMapping("/material-categories/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return BaseResponse.successData("OK");
    }

    @ApiOperation("Xoá danh sách loại vật liệu (ADMIN)")
    @DeleteMapping("/material-categories")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteBulk(@RequestParam List<Long> ids) {
        List<MaterialCategory> deleted = service.deleteBulk(ids);
        return BaseResponse.successListData(deleted, deleted.size());
    }
}
