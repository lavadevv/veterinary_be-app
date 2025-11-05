package ext.vnua.veterinary_beapp.modules.production.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionOrderMaterialDto {
    private Long id;
    private Long productionOrderId;
    private String orderCode;

    // Material Batch Item info
    private Long materialBatchItemId;
    private String materialBatchItemCode;
    
    // Parent Batch info
    private Long materialBatchId;
    private String materialBatchNumber;

    // Material info
    private Long materialId;
    private String materialCode;
    private String materialName;

    private BigDecimal requiredQuantity;
    private BigDecimal issuedQuantity;
    private BigDecimal actualQuantity;

    private String status;
    private String notes;
}
