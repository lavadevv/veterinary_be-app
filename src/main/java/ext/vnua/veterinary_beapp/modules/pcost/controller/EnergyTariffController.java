// ext/vnua/veterinary_beapp/modules/pcost/controller/EnergyTariffController.java
package ext.vnua.veterinary_beapp.modules.pcost.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.pcost.dto.GetEnergyTariffRequest;
import ext.vnua.veterinary_beapp.modules.pcost.model.EnergyTariff;
import ext.vnua.veterinary_beapp.modules.pcost.repository.custom.CustomEnergyTariffQuery;
import ext.vnua.veterinary_beapp.modules.pcost.service.EnergyTariffService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pcost/energy-tariffs")
@RequiredArgsConstructor
public class EnergyTariffController {
    private final EnergyTariffService service;

    @GetMapping @ApiOperation("Danh sách giá điện đang active")
    public ResponseEntity<List<EnergyTariff>> listActive() { return ResponseEntity.ok(service.listActive()); }

    @GetMapping("/{id}") @ApiOperation("Xem 1 giá điện")
    public ResponseEntity<EnergyTariff> get(@PathVariable Long id) { return ResponseEntity.ok(service.get(id)); }

    @PostMapping @ApiOperation("Tạo giá điện")
    public ResponseEntity<EnergyTariff> create(@RequestBody EnergyTariff r) { return ResponseEntity.ok(service.create(r)); }

    @PutMapping("/{id}") @ApiOperation("Cập nhật giá điện")
    public ResponseEntity<EnergyTariff> update(@PathVariable Long id, @RequestBody EnergyTariff r) { return ResponseEntity.ok(service.update(id, r)); }

    @DeleteMapping("/{id}") @ApiOperation("Xoá giá điện")
    public ResponseEntity<?> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.ok("Đã xoá"); }

    // ext/vnua/veterinary_beapp/modules/pcost/controller/EnergyTariffController.java
    @GetMapping("/search")
    @ApiOperation("Tìm kiếm + phân trang giá điện")
    public ResponseEntity<?> search(@Valid @ModelAttribute GetEnergyTariffRequest req) {
        var page = service.search(req, PageRequest.of(req.getStart(), req.getLimit()));
        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }

}
