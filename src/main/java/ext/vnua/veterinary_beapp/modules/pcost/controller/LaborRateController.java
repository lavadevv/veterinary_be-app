package ext.vnua.veterinary_beapp.modules.pcost.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.pcost.dto.GetLaborRate;
import ext.vnua.veterinary_beapp.modules.pcost.model.LaborRate;
import ext.vnua.veterinary_beapp.modules.pcost.repository.custom.CustomLaborRateQuery;
import ext.vnua.veterinary_beapp.modules.pcost.service.LaborRateService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pcost/labor-rates")
@RequiredArgsConstructor
public class LaborRateController {

    private final LaborRateService service;

    @GetMapping @ApiOperation("Danh sách đơn giá nhân công đang active")
    public ResponseEntity<java.util.List<LaborRate>> listActive() {
        return ResponseEntity.ok(service.listActive());
    }

    @GetMapping("/{id}") @ApiOperation("Xem 1 đơn giá nhân công")
    public ResponseEntity<LaborRate> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping @ApiOperation("Tạo đơn giá nhân công")
    public ResponseEntity<LaborRate> create(@RequestBody LaborRate r) {
        return ResponseEntity.ok(service.create(r));
    }

    @PutMapping("/{id}") @ApiOperation("Cập nhật đơn giá nhân công")
    public ResponseEntity<LaborRate> update(@PathVariable Long id, @RequestBody LaborRate r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @DeleteMapping("/{id}") @ApiOperation("Xoá đơn giá nhân công")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Đã xoá");
    }

    @GetMapping("/search") @ApiOperation("Tìm kiếm + paging đơn giá nhân công")
    public ResponseEntity<?> search(@Valid @ModelAttribute GetLaborRate req) {
        CustomLaborRateQuery.LaborRateFilterParam p = req;
        Page<LaborRate> page = service.search(p, PageRequest.of(req.getStart(), req.getLimit()));
        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }
}
