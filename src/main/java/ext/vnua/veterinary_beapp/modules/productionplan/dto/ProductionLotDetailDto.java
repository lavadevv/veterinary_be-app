package ext.vnua.veterinary_beapp.modules.productionplan.dto;

import ext.vnua.veterinary_beapp.modules.productionplan.enums.ProductionPlanStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Detailed view of a Production Lot with all its plans and products
 */
@Data
public class ProductionLotDetailDto {
    private Long id;
    private String lotNumber;
    private Integer sequenceInMonth;
    private Integer planMonth;
    private Integer planYear;
    private LocalDate planDate;
    private ProductionPlanStatus status;
    private String notes;
    
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
    
    private List<PlanProductRow> products = new ArrayList<>();
    
    /**
     * Represents one row in the detail table
     * Each row is a product from a plan
     */
    @Data
    public static class PlanProductRow {
        // From Plan
        private Long planId;
        private String formulaCode;
        private String formulaName;
        private BigDecimal batchSize;
        private Integer batchNumber; // Số lô (số thứ tự của plan trong lot)
        
        // From Product Line
        private Long productLineId;
        private Long productId;
        private String productCode;
        private String productName;
        private String unitOfMeasure;
        
        // Production Cost
        private Long productionCostSheetId;
        private String productionCostSheetCode;
        private String productionCostSheetName;
        private Integer specUnits; // Quy cách (định mức sản xuất)
        
        // Brand
        private String productBrand; // Thương hiệu
        
        // Quantities
        private BigDecimal plannedQuantity; // Số lượng kế hoạch
        private BigDecimal actualQuantity;  // Thực nhập
        
        // Cost
        private BigDecimal plannedUnitCost;
        private BigDecimal plannedTotalCost;
    }
}
