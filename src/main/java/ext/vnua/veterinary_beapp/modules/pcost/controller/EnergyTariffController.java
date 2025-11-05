// ext/vnua/veterinary_beapp/modules/pcost/controller/EnergyTariffController.java
package ext.vnua.veterinary_beapp.modules.pcost.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.pcost.dto.EnergyTariffDto;
import ext.vnua.veterinary_beapp.modules.pcost.dto.GetEnergyTariffRequest;
import ext.vnua.veterinary_beapp.modules.pcost.mapper.EnergyTariffMapper;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pcost/energy-tariffs")
@RequiredArgsConstructor
public class EnergyTariffController {
    private final EnergyTariffService service;
    private final EnergyTariffMapper mapper;

    @GetMapping @ApiOperation("Danh sách giá điện đang active")
    public ResponseEntity<List<EnergyTariffDto>> listActive() {
        List<EnergyTariff> tariffs = service.listActive();
        return ResponseEntity.ok(tariffs.stream().map(mapper::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}") @ApiOperation("Xem 1 giá điện")
    public ResponseEntity<EnergyTariffDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(service.get(id)));
    }

    @PostMapping @ApiOperation("Tạo giá điện")
    public ResponseEntity<EnergyTariffDto> create(@RequestBody EnergyTariff r) {
        return ResponseEntity.ok(mapper.toDto(service.create(r)));
    }

    @PutMapping("/{id}") @ApiOperation("Cập nhật giá điện")
    public ResponseEntity<EnergyTariffDto> update(@PathVariable Long id, @RequestBody EnergyTariff r) {
        return ResponseEntity.ok(mapper.toDto(service.update(id, r)));
    }

    @DeleteMapping("/{id}") @ApiOperation("Xoá giá điện")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Đã xoá");
    }

    @GetMapping("/search")
    @ApiOperation("Tìm kiếm + phân trang giá điện")
    public ResponseEntity<?> search(@Valid @ModelAttribute GetEnergyTariffRequest req) {
        Page<EnergyTariff> page = service.search(req, PageRequest.of(req.getStart(), req.getLimit()));
        List<EnergyTariffDto> dtos = page.getContent().stream().map(mapper::toDto).collect(Collectors.toList());
        return BaseResponse.successListData(dtos, (int) page.getTotalElements());
    }
}
