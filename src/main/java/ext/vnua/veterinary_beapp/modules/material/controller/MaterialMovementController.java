// File: ext/vnua/veterinary_beapp/modules/material/controller/MaterialMovementController.java
package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.material.dto.request.movement.*;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialMovement;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialMovementQuery;
import ext.vnua.veterinary_beapp.modules.material.service.InventoryMovementService;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialMovementQueryService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/material/movements")
@RequiredArgsConstructor
public class MaterialMovementController {

    private final MaterialMovementQueryService movementQueryService;
    private final InventoryMovementService inventoryMovementService;

    // ======================= QUERY (GET, @ModelAttribute, start/limit) =======================

    @ApiOperation("Tìm kiếm nhật ký di chuyển kho (paging start/limit, sort, filter)")
    @GetMapping
    public ResponseEntity<?> search(@Valid @ModelAttribute SearchMovementRequest req) {
        Page<MaterialMovement> page = movementQueryService.search(
                req, // req IS-A MovementFilterParam
                PageRequest.of(req.getStart(), req.getLimit())
        );
        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }

    // ======================= COMMANDS (ADMIN) =======================

    @ApiOperation("Xuất dùng từ 1 batch (ADMIN)")
    @PostMapping("/consume")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> consume(@Valid @RequestBody ConsumeFromBatchRequest req) {
        inventoryMovementService.consumeFromBatch(req.getBatchId(), req.getQuantity(), req.getNote());
        // successData(payload)
        return BaseResponse.successData(okMsg("xuất", req.getQuantity(), req.getBatchId(), null));
    }

    @ApiOperation("Chuyển toàn bộ batch sang vị trí khác (ADMIN)")
    @PostMapping("/transfer/all")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> moveAll(@Valid @RequestBody MoveBatchAllRequest req) {
        inventoryMovementService.moveBatchAll(req.getBatchId(), req.getToLocationId(), req.getNote());
        return BaseResponse.successData("Đã chuyển toàn bộ batch " + req.getBatchId()
                + " sang vị trí " + req.getToLocationId());
    }

    @ApiOperation("Tách/chuyển một phần batch sang vị trí khác (tạo batch mới) (ADMIN)")
    @PostMapping("/transfer/partial")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> movePartial(@Valid @RequestBody MoveBatchPartialRequest req) {
        Long newBatchId = inventoryMovementService.moveBatchPartially(
                req.getBatchId(), req.getToLocationId(), req.getQuantity(), req.getNote()
        );
        return BaseResponse.successData("Đã tách " + pretty(req.getQuantity())
                + " sang vị trí " + req.getToLocationId() + ", batch mới id=" + newBatchId);
    }

    @ApiOperation("Giữ chỗ một phần số lượng trong batch (ADMIN)")
    @PostMapping("/reserve")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> reserve(@Valid @RequestBody ReserveRequest req) {
        inventoryMovementService.reserve(req.getBatchId(), req.getQuantity(), req.getNote());
        return BaseResponse.successData(okMsg("giữ chỗ", req.getQuantity(), req.getBatchId(), null));
    }

    @ApiOperation("Hoàn/huỷ giữ chỗ số lượng trong batch (ADMIN)")
    @PostMapping("/release")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> release(@Valid @RequestBody ReleaseReserveRequest req) {
        inventoryMovementService.releaseReserve(req.getBatchId(), req.getQuantity(), req.getNote());
        return BaseResponse.successData(okMsg("hoàn giữ chỗ", req.getQuantity(), req.getBatchId(), null));
    }

    @ApiOperation("Đồng bộ lại sức chứa vị trí (ADMIN)")
    @PostMapping("/locations/{locationId}/recompute")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> recompute(@PathVariable Long locationId) {
        inventoryMovementService.recomputeLocationCapacity(locationId);
        return BaseResponse.successData("Đã đồng bộ sức chứa vị trí id=" + locationId);
    }

    // Helpers
    private String pretty(BigDecimal v) {
        return v == null ? "0" : v.stripTrailingZeros().toPlainString();
    }
    private String okMsg(String action, BigDecimal qty, Long batchId, Long locId) {
        String s = "Đã " + action + " " + pretty(qty) + " cho batch " + batchId;
        if (locId != null) s += " (vị trí " + locId + ")";
        return s;
    }
}
