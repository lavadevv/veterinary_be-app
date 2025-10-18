package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import lombok.Data;

@Data
public class OverheadCostDto {
    private Long id;
    private String code;
    private String title;
    private String type;
    private java.time.LocalDate costDate;
    private java.time.LocalDate periodMonth;
    private String unitOfMeasure;
    private java.math.BigDecimal quantity;
    private java.math.BigDecimal unitPrice;
    private java.math.BigDecimal amount;
    private String refNo;
    private String costCenter;
    private String note;
    private Long productId;
    private Long productBatchId;
    private String suggestedAllocation;
}