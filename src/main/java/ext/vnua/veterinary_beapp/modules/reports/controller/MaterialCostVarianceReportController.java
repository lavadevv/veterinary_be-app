package ext.vnua.veterinary_beapp.modules.reports.controller;

import ext.vnua.veterinary_beapp.modules.reports.dto.MaterialCostVarianceRequest;
import ext.vnua.veterinary_beapp.modules.reports.dto.MaterialCostVarianceResponse;
import ext.vnua.veterinary_beapp.modules.reports.services.MaterialCostVarianceReportService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class MaterialCostVarianceReportController {

    private final MaterialCostVarianceReportService service;

    /**
     * Ví dụ gọi:
     * POST /reports/material-cost-variance?includeOverheads=true
     * Body: { "year": 2025, "month": 7 }
     */
    @PostMapping("/material-cost-variance")
    @ApiOperation("Bảng chênh lệch giá nguyên liệu theo tháng (kèm chi phí ngoài nếu chọn)")
    public ResponseEntity<MaterialCostVarianceResponse> materialCostVariance(
            @Valid @RequestBody MaterialCostVarianceRequest req,
            @RequestParam(defaultValue = "true") boolean includeOverheads
    ) {
        return ResponseEntity.ok(service.buildReport(req, includeOverheads));
    }
}
