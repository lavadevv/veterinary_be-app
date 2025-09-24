package ext.vnua.veterinary_beapp.modules.production.services;

import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderMaterialDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.UpdateProductionOrderMaterialRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.materials.CreateProductionOrderMaterialRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductionOrderMaterialService {

    // Core CRUD operations
    ProductionOrderMaterialDto createMaterial(CreateProductionOrderMaterialRequest req);
    ProductionOrderMaterialDto updateMaterial(UpdateProductionOrderMaterialRequest req);
    void deleteMaterial(Long id);

    // Query operations
    ProductionOrderMaterialDto getById(Long id);
    List<ProductionOrderMaterialDto> getByOrder(Long orderId);
    Page<ProductionOrderMaterialDto> searchMaterials(Object filterParam, Pageable pageable);

    // Additional query methods
    List<ProductionOrderMaterialDto> getByOrderAndStatus(Long orderId, String status);

    // Status management operations
    void updateMaterialStatus(Long id, String status);
    void bulkUpdateMaterialStatus(List<Long> ids, String status);

    // Production workflow operations
    void issueMaterials(Long orderId, List<Long> materialIds);

    // Reporting and analytics
    BigDecimal getTotalRequiredQuantityByMaterial(Long materialId);
}