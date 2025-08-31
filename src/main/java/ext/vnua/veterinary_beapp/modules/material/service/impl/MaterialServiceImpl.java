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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {
    private final MaterialRepository materialRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialMapper materialMapper;

    @Override
    public Page<Material> getAllMaterial(CustomMaterialQuery.MaterialFilterParam param, PageRequest pageRequest) {
        Specification<Material> specification = CustomMaterialQuery.getFilterMaterial(param);
        return materialRepository.findAll(specification, pageRequest);
    }

    @Override
    public MaterialDto selectMaterialById(Long id) {
        Optional<Material> materialOptional = materialRepository.findById(id);
        if (materialOptional.isEmpty()) {
            throw new DataExistException("Vật liệu không tồn tại");
        }
        Material material = materialOptional.get();
        return materialMapper.toMaterialDto(material);
    }

    @Override
    public MaterialDto selectMaterialByCode(String materialCode) {
        Optional<Material> materialOptional = materialRepository.findByMaterialCode(materialCode);
        if (materialOptional.isEmpty()) {
            throw new DataExistException("Mã vật liệu không tồn tại");
        }
        Material material = materialOptional.get();
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
        // Validate supplier exists if provided
        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new DataExistException("Nhà cung cấp không tồn tại"));
        }

        // Validate material code is unique
        Optional<Material> existingMaterial = materialRepository.findByMaterialCode(request.getMaterialCode());
        if (existingMaterial.isPresent()) {
            throw new DataExistException("Mã vật liệu đã tồn tại");
        }

        // Validate material name is unique
        Optional<Material> existingMaterialName = materialRepository.findByMaterialName(request.getMaterialName());
        if (existingMaterialName.isPresent()) {
            throw new DataExistException("Tên vật liệu đã tồn tại");
        }

        // Validate stock levels
        if (request.getCurrentStock() != null && request.getCurrentStock() < 0) {
            throw new MyCustomException("Tồn kho hiện tại không được âm");
        }

        if (request.getMinimumStockLevel() != null && request.getMinimumStockLevel() < 0) {
            throw new MyCustomException("Mức tồn kho tối thiểu không được âm");
        }

        // Validate price
        if (request.getFixedPrice() != null && request.getFixedPrice() < 0) {
            throw new MyCustomException("Giá cố định không được âm");
        }

        // Validate percentage values
        if (request.getPurityPercentage() != null && (request.getPurityPercentage() < 0 || request.getPurityPercentage() > 100)) {
            throw new MyCustomException("Độ tinh khiết phải từ 0 đến 100%");
        }

        if (request.getMoistureContent() != null && (request.getMoistureContent() < 0 || request.getMoistureContent() > 100)) {
            throw new MyCustomException("Độ ẩm phải từ 0 đến 100%");
        }

        try {
            Material material = materialMapper.toCreateMaterial(request);
            if (supplier != null) {
                material.setSupplier(supplier);
            }
            material.setIsActive(true);

            // Set default current stock if not provided
            if (material.getCurrentStock() == null) {
                material.setCurrentStock(0.0);
            }

            return materialMapper.toMaterialDto(materialRepository.saveAndFlush(material));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm vật liệu");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "Material", description = "Cập nhật vật liệu")
    public MaterialDto updateMaterial(UpdateMaterialRequest request) {
        Optional<Material> materialOptional = materialRepository.findById(request.getId());
        if (materialOptional.isEmpty()) {
            throw new DataExistException("Vật liệu không tồn tại");
        }

        Material existingMaterial = materialOptional.get();

        // Validate supplier exists if changed
        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new DataExistException("Nhà cung cấp không tồn tại"));
        }

        // Validate material code is unique (excluding current material)
        if (!existingMaterial.getMaterialCode().equals(request.getMaterialCode())) {
            Optional<Material> duplicateMaterial = materialRepository
                    .findByMaterialCodeAndIdNot(request.getMaterialCode(), request.getId());
            if (duplicateMaterial.isPresent()) {
                throw new DataExistException("Mã vật liệu đã tồn tại");
            }
        }

        // Validate material name is unique (excluding current material)
        if (!existingMaterial.getMaterialName().equals(request.getMaterialName())) {
            Optional<Material> duplicateMaterialName = materialRepository
                    .findByMaterialNameAndIdNot(request.getMaterialName(), request.getId());
            if (duplicateMaterialName.isPresent()) {
                throw new DataExistException("Tên vật liệu đã tồn tại");
            }
        }

        // Validate stock levels
        if (request.getCurrentStock() != null && request.getCurrentStock() < 0) {
            throw new MyCustomException("Tồn kho hiện tại không được âm");
        }

        if (request.getMinimumStockLevel() != null && request.getMinimumStockLevel() < 0) {
            throw new MyCustomException("Mức tồn kho tối thiểu không được âm");
        }

        // Validate price
        if (request.getFixedPrice() != null && request.getFixedPrice() < 0) {
            throw new MyCustomException("Giá cố định không được âm");
        }

        // Validate percentage values
        if (request.getPurityPercentage() != null && (request.getPurityPercentage() < 0 || request.getPurityPercentage() > 100)) {
            throw new MyCustomException("Độ tinh khiết phải từ 0 đến 100%");
        }

        if (request.getMoistureContent() != null && (request.getMoistureContent() < 0 || request.getMoistureContent() > 100)) {
            throw new MyCustomException("Độ ẩm phải từ 0 đến 100%");
        }

        try {
            materialMapper.updateMaterialFromRequest(request, existingMaterial);
            if (supplier != null) {
                existingMaterial.setSupplier(supplier);
            }

            return materialMapper.toMaterialDto(materialRepository.saveAndFlush(existingMaterial));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật vật liệu");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Material", description = "Xóa vật liệu")
    public void deleteMaterial(Long id) {
        Optional<Material> materialOptional = materialRepository.findById(id);
        if (materialOptional.isEmpty()) {
            throw new DataExistException("Vật liệu không tồn tại");
        }

        Material material = materialOptional.get();

        // Check if material has batches
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
            Optional<Material> optionalMaterial = materialRepository.findById(id);
            if (optionalMaterial.isPresent()) {
                Material material = optionalMaterial.get();

                // Check if material has batches
                if (material.getBatches() != null && !material.getBatches().isEmpty()) {
                    throw new MyCustomException("Không thể xóa vật liệu đang có lô hàng: " + material.getMaterialName());
                }

                materialDtos.add(materialMapper.toMaterialDto(material));
                materialRepository.delete(material);
            } else {
                throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách vật liệu!");
            }
        }
        return materialDtos;
    }

    @Override
    @Transactional
    public void updateCurrentStock(Long materialId, Double newStock) {
        Optional<Material> materialOptional = materialRepository.findById(materialId);
        if (materialOptional.isEmpty()) {
            throw new DataExistException("Vật liệu không tồn tại");
        }

        if (newStock < 0) {
            throw new MyCustomException("Tồn kho hiện tại không được âm");
        }

        Material material = materialOptional.get();
        material.setCurrentStock(newStock);

        materialRepository.saveAndFlush(material);
    }

    @Override
    @Transactional
    public void toggleActiveStatus(Long materialId) {
        Optional<Material> materialOptional = materialRepository.findById(materialId);
        if (materialOptional.isEmpty()) {
            throw new DataExistException("Vật liệu không tồn tại");
        }

        Material material = materialOptional.get();
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
}
