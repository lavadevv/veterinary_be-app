package ext.vnua.veterinary_beapp.modules.production.dto;

import ext.vnua.veterinary_beapp.modules.production.enums.ProductionOrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductionOrderDto {
    private Long id;
    private String orderCode;

    private Long productId;
    private String productCode;
    private String productName;

    private Long productionLineId;
    private String productionLineCode;
    private String productionLineName;

    private BigDecimal plannedQuantity;
    private BigDecimal actualQuantity;
    private ProductionOrderStatus status;

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;

    private Long createdById;
    private String createdByName;   // ✅ đổi từ Username sang Name

    private Long approvedById;
    private String approvedByName;  // ✅ đổi từ Username sang Name

    private BigDecimal yieldRate;
    private String notes;
}
