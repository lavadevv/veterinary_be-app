package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.modules.material.dto.request.cost.CreateOverheadCostRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.cost.UpdateOverheadCostRequest;
import ext.vnua.veterinary_beapp.modules.material.model.OverheadCost;
import ext.vnua.veterinary_beapp.modules.material.service.OverheadCostService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/overhead-costs")
@RequiredArgsConstructor
public class OverheadCostController {

    private final OverheadCostService service;

    @ApiOperation("Tạo mới chi phí ngoài")
    @PostMapping
    public ResponseEntity<OverheadCost> create(@Valid @RequestBody CreateOverheadCostRequest req) {
        OverheadCost created = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @ApiOperation("Sửa chi phí ngoài")
    @PutMapping("/{id}")
    public ResponseEntity<OverheadCost> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateOverheadCostRequest req) {
        req.setId(id);
        return ResponseEntity.ok(service.update(req));
    }

    @ApiOperation("Xoá chi phí ngoài theo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Đã xoá chi phí: " + id);
    }

    @ApiOperation("Xem chi tiết chi phí ngoài theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<OverheadCost> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @ApiOperation("Liệt kê chi phí ngoài theo khoảng ngày (đóng chứng từ)")
    @GetMapping
    public ResponseEntity<List<OverheadCost>> listByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(service.listByDateRange(from, to));
    }

    @ApiOperation("Liệt kê chi phí ngoài theo kỳ tháng (YYYY-MM-01)")
    @GetMapping("/period")
    public ResponseEntity<List<OverheadCost>> listByPeriod(
            @RequestParam("month") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodMonth) {
        // ví dụ client gửi 2025-07-01; service sẽ .withDayOfMonth(1) cho chắc
        return ResponseEntity.ok(service.listByPeriod(periodMonth));
    }
}
