package ext.vnua.veterinary_beapp.modules.material.controller;

import ext.vnua.veterinary_beapp.modules.material.dto.request.formulaPrice.UpdateFormulaPriceRequest;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialPriceHistory;
import ext.vnua.veterinary_beapp.modules.material.service.FormulaPriceService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/materials/formula-price")
@RequiredArgsConstructor
public class FormulaPriceController {

    private final FormulaPriceService service;

    @PutMapping("")
    @ApiOperation("Cập nhật giá công thức & ghi lịch sử")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateFormulaPriceRequest req) {
        service.updateFormulaPrice(req);
        return ResponseEntity.ok("Đã cập nhật giá & lưu lịch sử");
    }

    @GetMapping("/{materialId}/history")
    @ApiOperation("Xem lịch sử giá công thức của NVL")
    public ResponseEntity<List<MaterialPriceHistory>> history(@PathVariable Long materialId) {
        return ResponseEntity.ok(service.getPriceHistory(materialId));
    }
}
