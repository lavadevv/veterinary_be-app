// File: ext/vnua/veterinary_beapp/modules/material/service/impl/MaterialServiceImpl.java
package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.CreateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.UpdateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.MaterialMapper;
import ext.vnua.veterinary_beapp.modules.material.model.*;
import ext.vnua.veterinary_beapp.modules.material.repository.*;
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
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    // NEW: repositories cho master mới
    private final MaterialCategoryRepository materialCategoryRepository;
    private final MaterialFormTypeRepository materialFormTypeRepository;
    
    // Active Ingredients repositories
    private final ActiveIngredientRepository activeIngredientRepository;
    private final MaterialActiveIngredientRepository materialActiveIngredientRepository;

    private final MaterialMapper materialMapper;

    @Override
    public Page<Material> getAllMaterial(CustomMaterialQuery.MaterialFilterParam param, PageRequest pageRequest) {
        Specification<Material> specification = CustomMaterialQuery.getFilterMaterial(param);
        return materialRepository.findAll(specification, pageRequest);
    }

    @Override
    public MaterialDto selectMaterialById(Long id) {
        // Use method with active ingredients for full data loading
        Material material = materialRepository.findByIdWithActiveIngredients(id)
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));
        return materialMapper.toMaterialDto(material);
    }

    @Override
    public MaterialDto selectMaterialByCode(String materialCode) {
        // Use findByMaterialCode with EntityGraph for active ingredients
        Material material = materialRepository.findByMaterialCode(materialCode)
                .orElseThrow(() -> new DataExistException("Mã vật liệu không tồn tại"));
        return materialMapper.toMaterialDto(material);
    }

    @Override
    public List<MaterialDto> selectMaterialsBySupplier(Long supplierId) {
        List<Material> materials = materialRepository.findBySupplierId(supplierId);
        return materials.stream().map(materialMapper::toMaterialDto).toList();
    }

    @Override
    public List<?> getMaterialActiveIngredients(Long materialId) {
        // Verify material exists
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));
        
        // Get active ingredients with details
        List<MaterialActiveIngredient> activeIngredients = materialActiveIngredientRepository
                .findByMaterialIdWithActiveIngredient(materialId);
        
        // Map to simple DTO for dropdown
        return activeIngredients.stream()
                .map(mai -> {
                    var dto = new Object() {
                        public final Long id = mai.getActiveIngredient().getId();
                        public final String ingredientName = mai.getActiveIngredient().getIngredientName();
                        public final String ingredientCode = mai.getActiveIngredient().getIngredientCode();
                        public final BigDecimal contentValue = mai.getContentValue();
                        public final String contentUnit = mai.getContentUnit();
                        public final String notes = mai.getNotes();
                    };
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "Material", description = "Tạo mới vật liệu")
    public MaterialDto createMaterial(CreateMaterialRequest request) {
        // ====== Validate/unique ======
        materialRepository.findByMaterialCode(request.getMaterialCode().trim())
                .ifPresent(x -> { throw new DataExistException("Mã vật liệu đã tồn tại"); });
        materialRepository.findByMaterialName(request.getMaterialName().trim())
                .ifPresent(x -> { throw new DataExistException("Tên vật liệu đã tồn tại"); });

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

        // ====== Resolve masters ======
        UnitOfMeasure uom = unitOfMeasureRepository.findById(request.getUnitOfMeasureId())
                .orElseThrow(() -> new MyCustomException("Đơn vị đo không tồn tại"));

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new DataExistException("Nhà cung cấp không tồn tại"));
        }

        MaterialCategory category = materialCategoryRepository.findById(request.getMaterialCategoryId())
                .orElseThrow(() -> new MyCustomException("Loại vật liệu không tồn tại"));

        MaterialFormType formType = null;
        if (request.getMaterialFormTypeId() != null) {
            formType = materialFormTypeRepository.findById(request.getMaterialFormTypeId())
                    .orElseThrow(() -> new MyCustomException("Dạng vật liệu không tồn tại"));
        }

        try {
            // KHÔNG rely mapper cho master vừa đổi schema — map tay an toàn hơn
            Material material = new Material();
            material.setMaterialCode(request.getMaterialCode().trim());
            material.setMaterialName(request.getMaterialName().trim());
            material.setInternationalName(request.getInternationalName());

            material.setMaterialCategory(category);
            material.setMaterialFormType(formType);

            material.setPurityPercentage(request.getPurityPercentage());
            material.setIuPerGram(request.getIuPerGram());
            material.setColor(request.getColor());
            material.setOdor(request.getOdor());
            material.setMoistureContent(request.getMoistureContent());
            material.setViscosity(request.getViscosity());
            material.setUnitOfMeasure(uom);
            material.setStandardApplied(request.getStandardApplied());
            material.setSupplier(supplier);
            material.setMinimumStockLevel(request.getMinimumStockLevel());
            material.setFixedPrice(request.getFixedPrice());
            material.setRequiresColdStorage(Boolean.TRUE.equals(request.getRequiresColdStorage()));
            material.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
            material.setNotes(request.getNotes());

            // tồn kho tổng là dẫn xuất từ batch → mặc định 0 khi mới tạo
            material.setCurrentStock(BigDecimal.ZERO);

            Material saved = materialRepository.saveAndFlush(material);
            
            // Xử lý active ingredients
            if (request.getActiveIngredients() != null && !request.getActiveIngredients().isEmpty()) {
                createActiveIngredientRelations(saved, request.getActiveIngredients());
            }

            // sync lại (dù 0) để giữ hành vi cũ
            // TODO: Re-enable when syncMaterialStock is implemented
            // syncMaterialStock(saved.getId());

            return materialMapper.toMaterialDto(saved);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm vật liệu: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "Material", description = "Cập nhật vật liệu")
    public MaterialDto updateMaterial(UpdateMaterialRequest request) {
        Material existing = materialRepository.findById(request.getId())
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));

        // ====== Unique checks (exclude current) ======
        if (request.getMaterialCode() != null && !request.getMaterialCode().trim().isEmpty()
                && !request.getMaterialCode().equals(existing.getMaterialCode())) {
            materialRepository.findByMaterialCodeAndIdNot(request.getMaterialCode().trim(), request.getId())
                    .ifPresent(x -> { throw new DataExistException("Mã vật liệu đã tồn tại"); });
            existing.setMaterialCode(request.getMaterialCode().trim());
        }
        if (request.getMaterialName() != null && !request.getMaterialName().trim().isEmpty()
                && !request.getMaterialName().equals(existing.getMaterialName())) {
            materialRepository.findByMaterialNameAndIdNot(request.getMaterialName().trim(), request.getId())
                    .ifPresent(x -> { throw new DataExistException("Tên vật liệu đã tồn tại"); });
            existing.setMaterialName(request.getMaterialName().trim());
        }

        // ====== Numeric validations ======
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

        // ====== Resolve masters nếu có thay đổi ======
        if (request.getUnitOfMeasureId() != null) {
            UnitOfMeasure uom = unitOfMeasureRepository.findById(request.getUnitOfMeasureId())
                    .orElseThrow(() -> new MyCustomException("Đơn vị đo không tồn tại"));
            existing.setUnitOfMeasure(uom);
        }
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new DataExistException("Nhà cung cấp không tồn tại"));
            existing.setSupplier(supplier);
        }
        if (request.getMaterialCategoryId() != null) {
            MaterialCategory category = materialCategoryRepository.findById(request.getMaterialCategoryId())
                    .orElseThrow(() -> new MyCustomException("Loại vật liệu không tồn tại"));
            existing.setMaterialCategory(category);
        }
        if (request.getMaterialFormTypeId() != null) {
            MaterialFormType formType = materialFormTypeRepository.findById(request.getMaterialFormTypeId())
                    .orElseThrow(() -> new MyCustomException("Dạng vật liệu không tồn tại"));
            existing.setMaterialFormType(formType);
        }

        // ====== Map các scalar còn lại ======
        existing.setInternationalName(request.getInternationalName());
        existing.setPurityPercentage(request.getPurityPercentage());
        existing.setIuPerGram(request.getIuPerGram());
        existing.setColor(request.getColor());
        existing.setOdor(request.getOdor());
        existing.setMoistureContent(request.getMoistureContent());
        existing.setViscosity(request.getViscosity());
        existing.setStandardApplied(request.getStandardApplied());
        existing.setMinimumStockLevel(request.getMinimumStockLevel());
        existing.setFixedPrice(request.getFixedPrice());
        if (request.getRequiresColdStorage() != null) {
            existing.setRequiresColdStorage(request.getRequiresColdStorage());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        existing.setNotes(request.getNotes());

        try {
            // Không đụng currentStock (dẫn xuất)
            Material saved = materialRepository.saveAndFlush(existing);
            
            // Cập nhật active ingredients nếu có thay đổi
            if (request.getActiveIngredients() != null) {
                updateActiveIngredientRelations(saved, request.getActiveIngredients());
            }
            
            // Đồng bộ lại tồn kho từ lô
            // TODO: Re-enable when syncMaterialStock is implemented
            // syncMaterialStock(saved.getId());
            return materialMapper.toMaterialDto(saved);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật vật liệu: " + e.getMessage(), e);
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Material", description = "Xóa vật liệu")
    public void deleteMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));

        if (material.getBatchItems() != null && !material.getBatchItems().isEmpty()) {
            throw new MyCustomException("Không thể xóa vật liệu đang có lô hàng");
        }

        try {
            materialRepository.deleteById(id);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa vật liệu: " + e.getMessage(), e);
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Material", description = "Xóa danh sách vật liệu")
    public List<MaterialDto> deleteAllIdMaterials(List<Long> ids) {
        List<MaterialDto> materialDtos = new ArrayList<>();
        for (Long id : ids) {
            Material material = materialRepository.findById(id)
                    .orElseThrow(() -> new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách vật liệu!"));
            if (material.getBatchItems() != null && !material.getBatchItems().isEmpty()) {
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
     * TODO: Currently not functional - needs refactoring for MaterialBatchItem
     */
    @Override
    @Transactional
    public void updateCurrentStock(Long materialId, Double ignoredNewStock) {
        // syncMaterialStock(materialId);
        // Temporarily disabled - needs refactoring
    }

    @Override
    @Transactional
    public void toggleActiveStatus(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));

        material.setIsActive(!Boolean.TRUE.equals(material.getIsActive()));
        materialRepository.saveAndFlush(material);
    }

    @Override
    public List<MaterialDto> getLowStockMaterials() {
        List<Material> materials = materialRepository.findLowStockMaterials();
        return materials.stream().map(materialMapper::toMaterialDto).toList();
    }

    @Override
    public List<MaterialDto> getMaterialsRequiringColdStorage() {
        List<Material> materials = materialRepository.findByRequiresColdStorageTrue();
        return materials.stream().map(materialMapper::toMaterialDto).toList();
    }

    @Override
    public List<MaterialDto> getActiveMaterials() {
        List<Material> materials = materialRepository.findByIsActiveTrue();
        return materials.stream().map(materialMapper::toMaterialDto).toList();
    }

    @Override
    @Transactional
    public void syncMaterialStock(Long materialId) {
        // TODO: Refactor to sum quantities from MaterialBatchItem
        // Use MaterialBatchItemRepository.calculateTotalAvailableQuantity(materialId, locationId)
        throw new UnsupportedOperationException(
                "Stock sync needs to be refactored to work with MaterialBatchItem. " +
                "Use MaterialBatchItemRepository.calculateTotalAvailableQuantity() instead.");
    }

    private void createActiveIngredientRelations(Material material, List<ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.MaterialActiveIngredientRequest> activeIngredients) {
        for (ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.MaterialActiveIngredientRequest request : activeIngredients) {
            ActiveIngredient activeIngredient = activeIngredientRepository.findById(request.getActiveIngredientId())
                    .orElseThrow(() -> new DataExistException("Hoạt chất với ID " + request.getActiveIngredientId() + " không tồn tại"));
            
            // Kiểm tra xem đã tồn tại chưa
            if (materialActiveIngredientRepository.existsByMaterialIdAndActiveIngredientId(
                    material.getId(), activeIngredient.getId())) {
                continue; // Skip nếu đã tồn tại
            }
            
            MaterialActiveIngredient mai = new MaterialActiveIngredient();
            mai.setMaterial(material);
            mai.setActiveIngredient(activeIngredient);
            mai.setContentValue(request.getContentValue());
            mai.setContentUnit(request.getContentUnit());
            mai.setNotes(request.getNotes());
            
            materialActiveIngredientRepository.save(mai);
        }
    }

    private void updateActiveIngredientRelations(Material material, List<ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.MaterialActiveIngredientRequest> activeIngredients) {
        // Xóa tất cả active ingredients hiện tại
        materialActiveIngredientRepository.deleteByMaterialId(material.getId());
        
        // Thêm lại active ingredients mới
        if (activeIngredients != null && !activeIngredients.isEmpty()) {
            createActiveIngredientRelations(material, activeIngredients);
        }
    }
}
