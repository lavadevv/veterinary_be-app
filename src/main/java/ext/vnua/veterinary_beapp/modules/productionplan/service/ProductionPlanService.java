package ext.vnua.veterinary_beapp.modules.productionplan.service;

import ext.vnua.veterinary_beapp.modules.productionplan.dto.MaterialRequirementDto;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionPlanDto;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionPlanFormulaContextDto;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionLotDto;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.CalculateMaterialRequirementRequest;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.CreateProductionLotRequest;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.GetProductionPlanRequest;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.ProductionPlanListRow;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.UpdateProductionPlanRequest;
import ext.vnua.veterinary_beapp.modules.productionplan.repository.custom.CustomProductionPlanQuery;
import ext.vnua.veterinary_beapp.modules.productionplan.repository.custom.CustomProductionLotQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

public interface ProductionPlanService {

    /** Batch create under a new ProductionLot (single transaction). */
    java.util.List<ProductionPlanDto> createPlansBatch(CreateProductionLotRequest request);

    ProductionPlanDto getPlan(Long id);

    Page<ProductionPlanDto> searchPlans(GetProductionPlanRequest request, Pageable pageable);

    ProductionPlanDto updatePlan(Long id, UpdateProductionPlanRequest request);

    void deletePlan(Long id);

    ProductionPlanFormulaContextDto getFormulaContext(Long formulaId);

    /** Trả về danh sách rút gọn (ListRow) theo filter để FE render nhanh */
    Page<ProductionPlanListRow> getAllPlanRows(CustomProductionPlanQuery.ProductionPlanFilterParam param, PageRequest pageRequest);

    // Lots (grouped view)
    Page<ProductionLotDto> searchLots(CustomProductionLotQuery.ProductionLotFilterParam param, PageRequest pageRequest);
    ProductionLotDto getLot(Long id);
    
    /** Get detailed view of a lot with all plans and products */
    ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionLotDetailDto getLotDetail(Long id);
    
    /** Calculate material requirements for a formula with given batch size */
    MaterialRequirementDto calculateMaterialRequirements(CalculateMaterialRequirementRequest request);
    
    /** Simulate FIFO material consumption from MaterialBatch inventory */
    ext.vnua.veterinary_beapp.modules.productionplan.dto.MaterialBatchConsumptionDto simulateMaterialConsumption(
            ext.vnua.veterinary_beapp.modules.productionplan.dto.request.SimulateMaterialConsumptionRequest request
    );
    
    /** Get production cost materials for a lot (Lệnh xuất vật liệu) */
    ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionCostMaterialDto getProductionCostMaterials(Long lotId);
}
