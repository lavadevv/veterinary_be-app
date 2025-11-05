package ext.vnua.veterinary_beapp.modules.productionplan.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for "Lệnh xuất vật liệu" (Production Cost Material Release Order)
 * Lists all materials, labor, and energy needed based on ProductionCostSheet
 */
@Data
public class ProductionCostMaterialDto {
    
    // Lot information
    private Long lotId;
    private String lotNumber;
    
    // Formula information (first product's formula)
    private String formulaCode;
    private String formulaName;
    private BigDecimal totalBatchSize;
    private String batchUnit;
    
    // Product costs breakdown
    private List<ProductCostDetail> productCosts = new ArrayList<>();
    
    // Summary
    private Summary summary;
    
    /**
     * Cost detail for each product in the lot
     */
    @Data
    public static class ProductCostDetail {
        private Long productId;
        private String productCode;
        private String productName;
        private BigDecimal plannedQuantity;
        private String unitOfMeasure;
        
        // Cost sheet info
        private CostSheetInfo costSheet;
        
        // Cost items
        private List<CostMaterialItem> items = new ArrayList<>();
    }
    
    /**
     * Cost sheet brief info
     */
    @Data
    public static class CostSheetInfo {
        private Long id;
        private String sheetCode;
        private String sheetName;
        private Integer specUnits; // 1 cost sheet cho bao nhiêu SP
    }
    
    /**
     * Individual cost item (material/labor/energy)
     */
    @Data
    public static class CostMaterialItem {
        private Integer orderNo;
        private String itemType; // MATERIAL, LABOR, ENERGY
        private String itemCode;
        private String itemName;
        private String unit;
        
        // Quantity calculation
        private BigDecimal baseQuantity;    // Quantity in cost sheet
        private BigDecimal scaledQuantity;  // baseQuantity × scaleFactor
        private BigDecimal scaleFactor;     // plannedQuantity / specUnits
        
        // Additional info
        private String notes;
    }
    
    /**
     * Summary statistics
     */
    @Data
    public static class Summary {
        private Integer totalProducts;
        private Integer totalItems;
        private Integer materialItems;
        private Integer laborItems;
        private Integer energyItems;
    }
}
