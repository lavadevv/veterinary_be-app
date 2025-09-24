package ext.vnua.veterinary_beapp.modules.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductBatchConsumptionDto {
    private Long id;
    private Long productBatchId;
    private String productBatchNumber;

    private Long materialBatchId;
    private String materialCode;
    private String materialBatchNumber;

    private BigDecimal plannedQuantity;
    private BigDecimal actualQuantity;
}