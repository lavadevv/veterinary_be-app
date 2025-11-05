package ext.vnua.veterinary_beapp.modules.productionplan.controller;

import ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionPlanDto;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionPlanFormulaContextDto;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.CreateProductionLotRequest;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.GetProductionPlanRequest;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.UpdateProductionPlanRequest;
import ext.vnua.veterinary_beapp.modules.productionplan.service.ProductionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/production/plans")
@Validated
@RequiredArgsConstructor
public class ProductionPlanController {

    private final ProductionPlanService productionPlanService;

    /** Batch create plans under a new lot */
    @PostMapping("/batch")
    public ResponseEntity<?> createPlansBatch(@Valid @RequestBody CreateProductionLotRequest request) {
        var dtos = productionPlanService.createPlansBatch(request);
        return BaseResponse.successListData(dtos, dtos.size());
    }

    @GetMapping("/{id}")
    public ProductionPlanDto getPlan(@PathVariable Long id) {
        return productionPlanService.getPlan(id);
    }

    @GetMapping
    public ResponseEntity<?> searchPlans(@Valid @ModelAttribute GetProductionPlanRequest filter,
                                         @PageableDefault Pageable pageable) {
        Page<ProductionPlanDto> page = productionPlanService.searchPlans(filter, pageable);
        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }

    @PutMapping("/{id}")
    public ProductionPlanDto updatePlan(@PathVariable Long id,
                                        @Valid @RequestBody UpdateProductionPlanRequest request) {
        return productionPlanService.updatePlan(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlan(@PathVariable Long id) {
        productionPlanService.deletePlan(id);
    }

    @GetMapping("/formula/{formulaId}/context")
    public ProductionPlanFormulaContextDto getFormulaContext(@PathVariable Long formulaId) {
        return productionPlanService.getFormulaContext(formulaId);
    }

    /**
     * Calculate material requirements for production order
     * Input: formulaId + batchSize
     * Output: List of materials with quantities and costs
     */
    @PostMapping("/calculate-materials")
    public ResponseEntity<?> calculateMaterialRequirements(
            @Valid @RequestBody ext.vnua.veterinary_beapp.modules.productionplan.dto.request.CalculateMaterialRequirementRequest request
    ) {
        var result = productionPlanService.calculateMaterialRequirements(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Simulate FIFO material consumption from MaterialBatch inventory
     * Input: formulaId + batchSize (+ optional lotId)
     * Output: Detailed breakdown of which MaterialBatches will be consumed
     */
    @PostMapping("/simulate-material-consumption")
    public ResponseEntity<?> simulateMaterialConsumption(
            @Valid @RequestBody ext.vnua.veterinary_beapp.modules.productionplan.dto.request.SimulateMaterialConsumptionRequest request
    ) {
        var result = productionPlanService.simulateMaterialConsumption(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Lightweight list for plans (ListRow) using custom query pattern.
     */
    @GetMapping("/list-rows")
    public ResponseEntity<?> listRows(
            @Valid @ModelAttribute GetProductionPlanRequest filter,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "20") int limit
    ) {
        var param = new ext.vnua.veterinary_beapp.modules.productionplan.repository.custom.CustomProductionPlanQuery.ProductionPlanFilterParam();
        param.setLotNumber(filter.getLotNumber());
        param.setKeywords(filter.getKeywords());
        param.setFormulaId(filter.getFormulaId());
        param.setProductId(filter.getProductId());
        param.setStatus(filter.getStatus());
        param.setFromDate(filter.getFromDate());
        param.setToDate(filter.getToDate());

        var page = productionPlanService.getAllPlanRows(param, PageRequest.of(start, limit));
        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }

    // ===== Lots (grouped view) =====
    @GetMapping("/lots")
    public ResponseEntity<?> listLots(
            @RequestParam(required = false) String lotNumber,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate toDate,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "20") int limit
    ) {
        var p = new ext.vnua.veterinary_beapp.modules.productionplan.repository.custom.CustomProductionLotQuery.ProductionLotFilterParam();
        p.setLotNumber(lotNumber);
        p.setKeywords(keywords);
        if (status != null) {
            try {
                p.setStatus(ext.vnua.veterinary_beapp.modules.productionplan.enums.ProductionPlanStatus.valueOf(status));
            } catch (IllegalArgumentException ignored) {}
        }
        p.setFromDate(fromDate);
        p.setToDate(toDate);
        var page = productionPlanService.searchLots(p, PageRequest.of(start, limit));
        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }

    @GetMapping("/lots/{id}")
    public ResponseEntity<?> getLot(@PathVariable Long id) {
        var dto = productionPlanService.getLot(id);
        return ResponseEntity.ok(dto);
    }
    
    /**
     * Get detailed view of a lot with all plans and products in table format
     */
    @GetMapping("/lots/{id}/detail")
    public ResponseEntity<?> getLotDetail(@PathVariable Long id) {
        var dto = productionPlanService.getLotDetail(id);
        return ResponseEntity.ok(dto);
    }
    
    /**
     * Get production cost materials for a lot (Lệnh xuất vật liệu)
     * Lists all materials, labor, and energy items from ProductionCostSheets
     */
    @GetMapping("/lots/{lotId}/cost-materials")
    public ResponseEntity<?> getProductionCostMaterials(@PathVariable Long lotId) {
        var result = productionPlanService.getProductionCostMaterials(lotId);
        return ResponseEntity.ok(result);
    }
}
