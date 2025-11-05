package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.ManufacturerDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.manufacturer.CreateManufacturerRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.manufacturer.GetManufacturerRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.manufacturer.UpdateManufacturerRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.ManufacturerMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Manufacturer;
import ext.vnua.veterinary_beapp.modules.material.service.ManufacturerService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/manufacturers")
@RequiredArgsConstructor
public class ManufacturerController {

    private final ManufacturerService manufacturerService;
    private final ManufacturerMapper manufacturerMapper;

    @ApiOperation("Danh sách NSX (paging/sort/search)")
    @GetMapping("")
    public ResponseEntity<?> list(@Valid @ModelAttribute GetManufacturerRequest req) {
        Page<Manufacturer> page = manufacturerService.getAllManufacturer(req, PageRequest.of(req.getStart(), req.getLimit()));
        var items = page.getContent().stream().map(manufacturerMapper::toManufacturerDto).collect(Collectors.toList());
        return BaseResponse.successListData(items, (int) page.getTotalElements());
    }

    @ApiOperation("Chi tiết NSX theo id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(manufacturerService.selectById(id));
    }

    @ApiOperation("Chi tiết NSX theo code")
    @GetMapping("/code/{code}")
    public ResponseEntity<?> getByCode(@PathVariable("code") String code) {
        return ResponseEntity.ok(manufacturerService.selectByCode(code));
    }

    @ApiOperation("Danh sách NSX đang hoạt động")
    @GetMapping("/active")
    public ResponseEntity<?> listActive() {
        return ResponseEntity.ok(manufacturerService.selectActive());
    }

    @ApiOperation("Tạo NSX")
    @PostMapping("")
    public ResponseEntity<?> create(@Valid @RequestBody CreateManufacturerRequest req) {
        ManufacturerDto dto = manufacturerService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @ApiOperation("Cập nhật NSX")
    @PutMapping("")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateManufacturerRequest req) {
        return ResponseEntity.ok(manufacturerService.update(req));
    }

    @ApiOperation("Bật/tắt hoạt động NSX")
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggle(@PathVariable Long id) {
        manufacturerService.toggleActive(id);
        return ResponseEntity.ok("OK");
    }

    @ApiOperation("Xoá NSX theo id")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        manufacturerService.delete(id);
        return ResponseEntity.ok("OK");
    }

    @ApiOperation("Xoá nhiều NSX")
    @DeleteMapping("/bulk")
    public ResponseEntity<?> deleteBulk(@RequestBody java.util.List<Long> ids) {
        var deleted = manufacturerService.deleteAllByIds(ids);
        return BaseResponse.successListData(deleted, deleted.size());
    }
}
