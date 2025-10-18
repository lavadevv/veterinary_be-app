package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.CreateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.UpdateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.MaterialMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.Supplier;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.SupplierRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialQuery;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {
    private final MaterialRepository materialRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialBatchRepository materialBatchRepository;
    private final MaterialMapper materialMapper;

    @Override
    public Page<Material> getAllMaterial(CustomMaterialQuery.MaterialFilterParam param, PageRequest pageRequest) {
        Specification<Material> specification = CustomMaterialQuery.getFilterMaterial(param);
        return materialRepository.findAll(specification, pageRequest);
    }

    @Override
    public MaterialDto selectMaterialById(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));
        return materialMapper.toMaterialDto(material);
    }

    @Override
    public MaterialDto selectMaterialByCode(String materialCode) {
        Material material = materialRepository.findByMaterialCode(materialCode)
                .orElseThrow(() -> new DataExistException("Mã vật liệu không tồn tại"));
        return materialMapper.toMaterialDto(material);
    }

    @Override
    public List<MaterialDto> selectMaterialsBySupplier(Long supplierId) {
        List<Material> materials = materialRepository.findBySupplierId(supplierId);
        return materials.stream()
                .map(materialMapper::toMaterialDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "Material", description = "Tạo mới vật liệu")
    public MaterialDto createMaterial(CreateMaterialRequest request) {
        // Validate supplier
        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new DataExistException("Nhà cung cấp không tồn tại"));
        }

        // Unique checks
        materialRepository.findByMaterialCode(request.getMaterialCode())
                .ifPresent(x -> { throw new DataExistException("Mã vật liệu đã tồn tại"); });

        materialRepository.findByMaterialName(request.getMaterialName())
                .ifPresent(x -> { throw new DataExistException("Tên vật liệu đã tồn tại"); });

        // Numeric validations (không đụng currentStock vì là derived)
        if (request.getMinimumStockLevel() != null
                && request.getMinimumStockLevel().compareTo(BigDecimal.ZERO) < 0) {
            throw new MyCustomException("Mức tồn kho tối thiểu không được âm");
        }
        if (request.getFixedPrice() != null && request.getFixedPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new MyCustomException("Giá cố định không được âm");
        }
        if (request.getPurityPercentage() != null) {
            if (request.getPurityPercentage().compareTo(BigDecimal.ZERO) < 0
                    || request.getPurityPercentage().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new MyCustomException("Độ tinh khiết phải từ 0 đến 100%");
            }
        }
        if (request.getMoistureContent() != null) {
            if (request.getMoistureContent().compareTo(BigDecimal.ZERO) < 0
                    || request.getMoistureContent().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new MyCustomException("Độ ẩm phải từ 0 đến 100%");
            }
        }

        try {
            Material material = materialMapper.toCreateMaterial(request);
            if (supplier != null) {
                material.setSupplier(supplier);
            }
            material.setIsActive(true);
            // currentStock là derived → mặc định 0 khi mới tạo (chưa có lô)
            if (material.getCurrentStock() == null) material.setCurrentStock(BigDecimal.ZERO);

            Material saved = materialRepository.saveAndFlush(material);

            // Đồng bộ tồn kho (thực ra sẽ vẫn = 0 khi mới tạo)
            syncMaterialStock(saved.getId());

            return materialMapper.toMaterialDto(saved);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm vật liệu");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "Material", description = "Cập nhật vật liệu")
    public MaterialDto updateMaterial(UpdateMaterialRequest request) {
        Material existingMaterial = materialRepository.findById(request.getId())
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));

        // Validate supplier
        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new DataExistException("Nhà cung cấp không tồn tại"));
        }

        // Unique checks (exclude current)
        if (!existingMaterial.getMaterialCode().equals(request.getMaterialCode())) {
            materialRepository.findByMaterialCodeAndIdNot(request.getMaterialCode(), request.getId())
                    .ifPresent(x -> { throw new DataExistException("Mã vật liệu đã tồn tại"); });
        }
        if (!existingMaterial.getMaterialName().equals(request.getMaterialName())) {
            materialRepository.findByMaterialNameAndIdNot(request.getMaterialName(), request.getId())
                    .ifPresent(x -> { throw new DataExistException("Tên vật liệu đã tồn tại"); });
        }

        // Numeric validations (không đụng currentStock vì là derived)
        if (request.getMinimumStockLevel() != null
                && request.getMinimumStockLevel().compareTo(BigDecimal.ZERO) < 0) {
            throw new MyCustomException("Mức tồn kho tối thiểu không được âm");
        }
        if (request.getFixedPrice() != null && request.getFixedPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new MyCustomException("Giá cố định không được âm");
        }
        if (request.getPurityPercentage() != null) {
            if (request.getPurityPercentage().compareTo(BigDecimal.ZERO) < 0
                    || request.getPurityPercentage().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new MyCustomException("Độ tinh khiết phải từ 0 đến 100%");
            }
        }
        if (request.getMoistureContent() != null) {
            if (request.getMoistureContent().compareTo(BigDecimal.ZERO) < 0
                    || request.getMoistureContent().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new MyCustomException("Độ ẩm phải từ 0 đến 100%");
            }
        }

        try {
            // Giữ lại tồn hiện tại trước khi map (tránh mapper overwrite)
            BigDecimal keepCurrentStock = existingMaterial.getCurrentStock();

            materialMapper.updateMaterialFromRequest(request, existingMaterial);
            if (supplier != null) {
                existingMaterial.setSupplier(supplier);
            }

            // Không cho mapper ghi đè currentStock
            existingMaterial.setCurrentStock(keepCurrentStock);

            Material saved = materialRepository.saveAndFlush(existingMaterial);

            // Đồng bộ lại tồn kho từ lô (phòng trường hợp thay đổi ảnh hưởng đến cách hiển thị/đơn vị)
            syncMaterialStock(saved.getId());

            return materialMapper.toMaterialDto(saved);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật vật liệu");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Material", description = "Xóa vật liệu")
    public void deleteMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));

        if (material.getBatches() != null && !material.getBatches().isEmpty()) {
            throw new MyCustomException("Không thể xóa vật liệu đang có lô hàng");
        }

        try {
            materialRepository.deleteById(id);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa vật liệu");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Material", description = "Xóa danh sách vật liệu")
    public List<MaterialDto> deleteAllIdMaterials(List<Long> ids) {
        List<MaterialDto> materialDtos = new ArrayList<>();
        for (Long id : ids) {
            Material material = materialRepository.findById(id)
                    .orElseThrow(() -> new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách vật liệu!"));

            if (material.getBatches() != null && !material.getBatches().isEmpty()) {
                throw new MyCustomException("Không thể xóa vật liệu đang có lô hàng: " + material.getMaterialName());
            }

            materialDtos.add(materialMapper.toMaterialDto(material));
            materialRepository.delete(material);
        }
        return materialDtos;
    }

    /**
     * Đồng bộ tồn kho tổng của Material từ các lô (MaterialBatch).
     * NOTE: giữ chữ ký cũ để không phá FE, nhưng bỏ ý nghĩa "set tay".
     */
    @Override
    @Transactional
    public void updateCurrentStock(Long materialId, Double ignoredNewStock) {
        syncMaterialStock(materialId);
    }

    @Override
    @Transactional
    public void toggleActiveStatus(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));

        material.setIsActive(!material.getIsActive());
        materialRepository.saveAndFlush(material);
    }

    @Override
    public List<MaterialDto> getLowStockMaterials() {
        List<Material> materials = materialRepository.findLowStockMaterials();
        return materials.stream()
                .map(materialMapper::toMaterialDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MaterialDto> getMaterialsRequiringColdStorage() {
        List<Material> materials = materialRepository.findByRequiresColdStorageTrue();
        return materials.stream()
                .map(materialMapper::toMaterialDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MaterialDto> getActiveMaterials() {
        List<Material> materials = materialRepository.findByIsActiveTrue();
        return materials.stream()
                .map(materialMapper::toMaterialDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void syncMaterialStock(Long materialId) {
        BigDecimal totalQuantity = materialBatchRepository.getTotalQuantityByMaterial(materialId);
        BigDecimal newStock = (totalQuantity != null ? totalQuantity : BigDecimal.ZERO);

        materialRepository.findById(materialId).ifPresent(m -> {
            m.setCurrentStock(newStock);
            materialRepository.saveAndFlush(m);
        });
    }
}
