package ext.vnua.veterinary_beapp.modules.users.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.entity.DepartmentDto;
import ext.vnua.veterinary_beapp.modules.users.dto.entity.PositionDto;
import ext.vnua.veterinary_beapp.modules.users.dto.request.CreateDepartmentRequest;
import ext.vnua.veterinary_beapp.modules.users.dto.request.CreatePositionRequest;
import ext.vnua.veterinary_beapp.modules.users.dto.request.GetDepartmentRequest;
import ext.vnua.veterinary_beapp.modules.users.dto.request.GetPositionRequest;
import ext.vnua.veterinary_beapp.modules.users.dto.request.UpdateDepartmentRequest;
import ext.vnua.veterinary_beapp.modules.users.dto.request.UpdatePositionRequest;
import ext.vnua.veterinary_beapp.modules.users.mapper.OrgMapper;
import ext.vnua.veterinary_beapp.modules.users.model.Department;
import ext.vnua.veterinary_beapp.modules.users.model.Position;
import ext.vnua.veterinary_beapp.modules.users.repository.CustomOrgQuery;
import ext.vnua.veterinary_beapp.modules.users.repository.DepartmentRepository;
import ext.vnua.veterinary_beapp.modules.users.repository.PositionRepository;
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
@RequestMapping("/user/org")
@RequiredArgsConstructor
public class OrganizationController {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final OrgMapper orgMapper;

    /* ===================== DEPARTMENTS ====================== */

    @ApiOperation("Danh sách phòng ban (Specification + paging/sort)")
    @GetMapping("/departments")
    public ResponseEntity<?> listDepartments(@Valid @ModelAttribute GetDepartmentRequest req) {
        Page<Department> page = departmentRepository.findAll(
                CustomOrgQuery.getFilterDepartment(req),
                PageRequest.of(req.getStart(), req.getLimit())
        );
        var items = page.getContent().stream().map(orgMapper::toDepartmentDto).toList();
        return BaseResponse.successListData(items, (int) page.getTotalElements());
    }

    @ApiOperation("Tạo phòng ban (ADMIN)")
    @PostMapping("/departments")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> createDepartment(@Valid @RequestBody CreateDepartmentRequest req) {
        Department d = new Department();
        d.setName(req.getName().trim());
        Department saved = departmentRepository.save(d);
        return ResponseEntity.ok(orgMapper.toDepartmentDto(saved));
    }

    @ApiOperation("Cập nhật phòng ban (ADMIN)")
    @PutMapping("/departments")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> updateDepartment(@Valid @RequestBody UpdateDepartmentRequest req) {
        Department d = departmentRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Department không tồn tại"));
        if (req.getName() != null) d.setName(req.getName().trim());
        Department saved = departmentRepository.save(d);
        return ResponseEntity.ok(orgMapper.toDepartmentDto(saved));
    }

    @ApiOperation("Xoá phòng ban (ADMIN)")
    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        try {
            departmentRepository.deleteById(id);
            return ResponseEntity.ok("OK");
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest().body("Không thể xoá: đang được tham chiếu bởi users");
        }
    }

    /* ======================= POSITIONS ====================== */

    @ApiOperation("Danh sách chức vụ (Specification + paging/sort)")
    @GetMapping("/positions")
    public ResponseEntity<?> listPositions(@Valid @ModelAttribute GetPositionRequest req) {
        Page<Position> page = positionRepository.findAll(
                CustomOrgQuery.getFilterPosition(req),
                PageRequest.of(req.getStart(), req.getLimit())
        );
        var items = page.getContent().stream().map(orgMapper::toPositionDto).toList();
        return BaseResponse.successListData(items, (int) page.getTotalElements());
    }

    @ApiOperation("Tạo chức vụ (ADMIN)")
    @PostMapping("/positions")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> createPosition(@Valid @RequestBody CreatePositionRequest req) {
        Position p = new Position();
        p.setName(req.getName().trim());
        Position saved = positionRepository.save(p);
        return ResponseEntity.ok(orgMapper.toPositionDto(saved));
    }

    @ApiOperation("Cập nhật chức vụ (ADMIN)")
    @PutMapping("/positions")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> updatePosition(@Valid @RequestBody UpdatePositionRequest req) {
        Position p = positionRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Position không tồn tại"));
        if (req.getName() != null) p.setName(req.getName().trim());
        Position saved = positionRepository.save(p);
        return ResponseEntity.ok(orgMapper.toPositionDto(saved));
    }

    @ApiOperation("Xoá chức vụ (ADMIN)")
    @DeleteMapping("/positions/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deletePosition(@PathVariable Long id) {
        try {
            positionRepository.deleteById(id);
            return ResponseEntity.ok("OK");
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest().body("Không thể xoá: đang được tham chiếu bởi users");
        }
    }
}
