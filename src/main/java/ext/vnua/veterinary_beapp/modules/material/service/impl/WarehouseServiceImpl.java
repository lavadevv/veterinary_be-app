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
import ext.vnua.veterinary_beapp.modules.material.model.WarehouseType;
import ext.vnua.veterinary_beapp.modules.material.repository.WarehouseRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.WarehouseTypeRepository;
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
    private final WarehouseTypeRepository warehouseTypeRepository;
    private final WarehouseMapper warehouseMapper;

    @Override
    public Page<Warehouse> getAllWarehouse(CustomWarehouseQuery.WarehouseFilterParam param, PageRequest pageRequest) {
        Specification<Warehouse> specification = CustomWarehouseQuery.getFilterWarehouse(param);
        return warehouseRepository.findAll(specification, pageRequest);
    }

    @Override
    public List<WarehouseDto> getAllWarehouses() {
        return warehouseRepository.findAll()
                .stream().map(warehouseMapper::toWarehouseDto).toList();
    }

    @Override
    public WarehouseDto selectWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Kho không tồn tại"));
        return warehouseMapper.toWarehouseDto(warehouse);
    }

    @Override
    public WarehouseDto selectWarehouseByCode(String warehouseCode) {
        Warehouse warehouse = warehouseRepository.findByWarehouseCode(warehouseCode)
                .orElseThrow(() -> new DataExistException("Mã kho không tồn tại"));
        return warehouseMapper.toWarehouseDto(warehouse);
    }

    @Override
    public List<WarehouseDto> selectAllActiveWarehouses() {
        return warehouseRepository.findByIsActiveTrue()
                .stream().map(warehouseMapper::toWarehouseDto).toList();
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "Warehouse", description = "Tạo mới kho")
    public WarehouseDto createWarehouse(CreateWarehouseRequest request) {
        warehouseRepository.findByWarehouseCode(request.getWarehouseCode())
                .ifPresent(w -> { throw new DataExistException("Mã kho đã tồn tại"); });

        if (request.getWarehouseName() == null || request.getWarehouseName().trim().isEmpty()) {
            throw new MyCustomException("Tên kho không được để trống");
        }
        if (request.getWarehouseCode() == null || request.getWarehouseCode().trim().isEmpty()) {
            throw new MyCustomException("Mã kho không được để trống");
        }

        try {
            Warehouse warehouse = warehouseMapper.toCreateWarehouse(request);
            warehouse.setIsActive(true);

            WarehouseType wt = warehouseTypeRepository.findById(request.getWarehouseTypeId())
                    .orElseThrow(() -> new MyCustomException("WarehouseType không tồn tại"));
            warehouse.setWarehouseType(wt);

            return warehouseMapper.toWarehouseDto(warehouseRepository.saveAndFlush(warehouse));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm kho");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "Warehouse", description = "Cập nhật kho")
    public WarehouseDto updateWarehouse(UpdateWarehouseRequest request) {
        Warehouse existing = warehouseRepository.findById(request.getId())
                .orElseThrow(() -> new DataExistException("Kho không tồn tại"));

        if (!existing.getWarehouseCode().equals(request.getWarehouseCode())) {
            warehouseRepository.findByWarehouseCodeAndIdNot(request.getWarehouseCode(), request.getId())
                    .ifPresent(w -> { throw new DataExistException("Mã kho đã tồn tại"); });
        }
        if (request.getWarehouseName() == null || request.getWarehouseName().trim().isEmpty()) {
            throw new MyCustomException("Tên kho không được để trống");
        }
        if (request.getWarehouseCode() == null || request.getWarehouseCode().trim().isEmpty()) {
            throw new MyCustomException("Mã kho không được để trống");
        }

        try {
            warehouseMapper.updateWarehouseFromRequest(request, existing);

            WarehouseType wt = warehouseTypeRepository.findById(request.getWarehouseTypeId())
                    .orElseThrow(() -> new MyCustomException("WarehouseType không tồn tại"));
            existing.setWarehouseType(wt);

            return warehouseMapper.toWarehouseDto(warehouseRepository.saveAndFlush(existing));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật kho");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Warehouse", description = "Xóa kho")
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Kho không tồn tại"));

        if (warehouse.getLocations() != null && !warehouse.getLocations().isEmpty()) {
            throw new MyCustomException("Không thể xóa kho đang có vị trí lưu trữ");
        }
        try {
            warehouseRepository.deleteById(id);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa kho");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Warehouse", description = "Xóa danh sách kho")
    public List<WarehouseDto> deleteAllIdWarehouses(List<Long> ids) {
        List<WarehouseDto> result = new ArrayList<>();
        for (Long id : ids) {
            Warehouse warehouse = warehouseRepository.findById(id)
                    .orElseThrow(() -> new MyCustomException("Kho không tồn tại: " + id));

            if (warehouse.getLocations() != null && !warehouse.getLocations().isEmpty()) {
                throw new MyCustomException("Không thể xóa kho đang có vị trí lưu trữ: " + warehouse.getWarehouseName());
            }
            result.add(warehouseMapper.toWarehouseDto(warehouse));
            warehouseRepository.delete(warehouse);
        }
        return result;
    }

    @Override
    @Transactional
    public void toggleActiveStatus(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new DataExistException("Kho không tồn tại"));
        warehouse.setIsActive(!Boolean.TRUE.equals(warehouse.getIsActive()));
        warehouseRepository.saveAndFlush(warehouse);
    }

    @Override
    public List<WarehouseDto> getWarehousesByTypeId(Long warehouseTypeId) {
        return warehouseRepository.findByWarehouseTypeIdAndIsActiveTrue(warehouseTypeId)
                .stream().map(warehouseMapper::toWarehouseDto).toList();
    }
}
