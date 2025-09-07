package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.WarehouseDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse.CreateWarehouseRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse.UpdateWarehouseRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.WarehouseMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Warehouse;
import ext.vnua.veterinary_beapp.modules.material.repository.WarehouseRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomWarehouseQuery;
import ext.vnua.veterinary_beapp.modules.material.service.WarehouseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    @Override
    public Page<Warehouse> getAllWarehouse(CustomWarehouseQuery.WarehouseFilterParam param, PageRequest pageRequest) {
        Specification<Warehouse> specification = CustomWarehouseQuery.getFilterWarehouse(param);
        return warehouseRepository.findAll(specification, pageRequest);
    }

    public List<WarehouseDto> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        return warehouses.stream()
                .map(warehouseMapper::toWarehouseDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public WarehouseDto selectWarehouseById(Long id) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(String.valueOf(id));
        if (warehouseOptional.isEmpty()) {
            throw new DataExistException("Kho không tồn tại");
        }
        Warehouse warehouse = warehouseOptional.get();
        return warehouseMapper.toWarehouseDto(warehouse);
    }

    @Override
    public WarehouseDto selectWarehouseByCode(String warehouseCode) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findByWarehouseCode(warehouseCode);
        if (warehouseOptional.isEmpty()) {
            throw new DataExistException("Mã kho không tồn tại");
        }
        Warehouse warehouse = warehouseOptional.get();
        return warehouseMapper.toWarehouseDto(warehouse);
    }

    @Override
    public List<WarehouseDto> selectAllActiveWarehouses() {
        List<Warehouse> warehouses = warehouseRepository.findByIsActiveTrue();
        return warehouses.stream()
                .map(warehouseMapper::toWarehouseDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "Warehouse", description = "Tạo mới kho")
    public WarehouseDto createWarehouse(CreateWarehouseRequest request) {
        // Validate warehouse code is unique
        Optional<Warehouse> existingWarehouse = warehouseRepository.findByWarehouseCode(request.getWarehouseCode());
        if (existingWarehouse.isPresent()) {
            throw new DataExistException("Mã kho đã tồn tại");
        }

        // Validate required fields
        if (request.getWarehouseName() == null || request.getWarehouseName().trim().isEmpty()) {
            throw new MyCustomException("Tên kho không được để trống");
        }

        if (request.getWarehouseCode() == null || request.getWarehouseCode().trim().isEmpty()) {
            throw new MyCustomException("Mã kho không được để trống");
        }

        try {
            Warehouse warehouse = warehouseMapper.toCreateWarehouse(request);
            warehouse.setIsActive(true);

            return warehouseMapper.toWarehouseDto(warehouseRepository.saveAndFlush(warehouse));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm kho");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "Warehouse", description = "Cập nhật kho")
    public WarehouseDto updateWarehouse(UpdateWarehouseRequest request) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(String.valueOf(request.getId()));
        if (warehouseOptional.isEmpty()) {
            throw new DataExistException("Kho không tồn tại");
        }

        Warehouse existingWarehouse = warehouseOptional.get();

        // Validate warehouse code is unique (excluding current warehouse)
        if (!existingWarehouse.getWarehouseCode().equals(request.getWarehouseCode())) {
            Optional<Warehouse> duplicateWarehouse = warehouseRepository.findByWarehouseCodeAndIdNot(
                    request.getWarehouseCode(), request.getId());
            if (duplicateWarehouse.isPresent()) {
                throw new DataExistException("Mã kho đã tồn tại");
            }
        }

        // Validate required fields
        if (request.getWarehouseName() == null || request.getWarehouseName().trim().isEmpty()) {
            throw new MyCustomException("Tên kho không được để trống");
        }

        if (request.getWarehouseCode() == null || request.getWarehouseCode().trim().isEmpty()) {
            throw new MyCustomException("Mã kho không được để trống");
        }

        try {
            warehouseMapper.updateWarehouseFromRequest(request, existingWarehouse);
            return warehouseMapper.toWarehouseDto(warehouseRepository.saveAndFlush(existingWarehouse));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật kho");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Warehouse", description = "Xóa kho")
    public void deleteWarehouse(Long id) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(String.valueOf(id));
        if (warehouseOptional.isEmpty()) {
            throw new DataExistException("Kho không tồn tại");
        }

        Warehouse warehouse = warehouseOptional.get();

        // Check if warehouse has locations
        if (warehouse.getLocations() != null && !warehouse.getLocations().isEmpty()) {
            throw new MyCustomException("Không thể xóa kho đang có vị trí lưu trữ");
        }

        try {
            warehouseRepository.deleteById(String.valueOf(id));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa kho");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Warehouse", description = "Xóa danh sách kho")
    public List<WarehouseDto> deleteAllIdWarehouses(List<Long> ids) {
        List<WarehouseDto> warehouseDtos = new ArrayList<>();
        for (Long id : ids) {
            Optional<Warehouse> optionalWarehouse = warehouseRepository.findById(String.valueOf(id));
            if (optionalWarehouse.isPresent()) {
                Warehouse warehouse = optionalWarehouse.get();

                // Check if warehouse has locations
                if (warehouse.getLocations() != null && !warehouse.getLocations().isEmpty()) {
                    throw new MyCustomException("Không thể xóa kho đang có vị trí lưu trữ: " + warehouse.getWarehouseName());
                }

                warehouseDtos.add(warehouseMapper.toWarehouseDto(warehouse));
                warehouseRepository.delete(warehouse);
            } else {
                throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách kho!");
            }
        }
        return warehouseDtos;
    }

    @Override
    @Transactional
    public void toggleActiveStatus(Long warehouseId) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(String.valueOf(warehouseId));
        if (warehouseOptional.isEmpty()) {
            throw new DataExistException("Kho không tồn tại");
        }

        Warehouse warehouse = warehouseOptional.get();
        warehouse.setIsActive(!warehouse.getIsActive());

        warehouseRepository.saveAndFlush(warehouse);
    }

    @Override
    public List<WarehouseDto> getWarehousesByType(String warehouseType) {
        List<Warehouse> warehouses = warehouseRepository.findByWarehouseTypeAndIsActiveTrue(warehouseType);
        return warehouses.stream()
                .map(warehouseMapper::toWarehouseDto)
                .collect(java.util.stream.Collectors.toList());
    }
}

