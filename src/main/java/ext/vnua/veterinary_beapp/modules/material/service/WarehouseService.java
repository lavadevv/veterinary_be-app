package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.WarehouseDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse.CreateWarehouseRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse.UpdateWarehouseRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Warehouse;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomWarehouseQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface WarehouseService {
    Page<Warehouse> getAllWarehouse(CustomWarehouseQuery.WarehouseFilterParam param, PageRequest pageRequest);
    WarehouseDto selectWarehouseById(Long id);
    WarehouseDto selectWarehouseByCode(String warehouseCode);
    List<WarehouseDto> selectAllActiveWarehouses();

    WarehouseDto createWarehouse(CreateWarehouseRequest request);
    WarehouseDto updateWarehouse(UpdateWarehouseRequest request);

    void deleteWarehouse(Long id);
    List<WarehouseDto> deleteAllIdWarehouses(List<Long> ids);

    // Additional business methods
    void toggleActiveStatus(Long warehouseId);
    List<WarehouseDto> getWarehousesByType(String warehouseType);
}
