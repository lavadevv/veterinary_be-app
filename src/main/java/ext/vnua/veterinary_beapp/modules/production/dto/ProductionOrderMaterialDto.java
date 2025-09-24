package ext.vnua.veterinary_beapp.modules.production.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionOrderMaterialDto {
    private Long id;
    private Long productionOrderId;
    private String orderCode;

    private Long materialBatchId;
    private String materialBatchNumber;

    private Long materialId;
    private String materialCode;
    private String materialName;

    private BigDecimal requiredQuantity;
    private BigDecimal issuedQuantity;
    private BigDecimal actualQuantity;

    private String status;
    private String notes;
}
