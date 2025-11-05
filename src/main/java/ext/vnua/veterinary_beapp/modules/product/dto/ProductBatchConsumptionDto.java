package ext.vnua.veterinary_beapp.modules.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductBatchConsumptionDto {
    private Long id;
    private Long productBatchId;
    private String productBatchNumber;

    // MaterialBatchItem info
    private Long materialBatchItemId;
    private String materialBatchItemCode;
    private String materialCode;
    
    // Parent batch info
    private Long materialBatchId;
    private String materialBatchNumber;

    private BigDecimal plannedQuantity;
    private BigDecimal actualQuantity;
}