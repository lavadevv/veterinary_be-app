package ext.vnua.veterinary_beapp.modules.productionplan.dto;

import ext.vnua.veterinary_beapp.modules.productionplan.enums.ProductionPlanStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductionPlanDto {
    private Long id;
    private String lotNumber;
    private Integer sequenceInMonth;
    private Integer planMonth;
    private Integer planYear;
    private LocalDate planDate;
    private BigDecimal batchSize;
    private ProductionPlanStatus status;

    private Long formulaId;
    private String formulaCode;
    private String formulaName;
    private String formulaVersion;

    private String notes;

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;

    private List<ProductLineDto> products;

    @Data
    public static class ProductLineDto {
        private Long id;
        private Long productId;
        private String productCode;
        private String productName;

        private BigDecimal plannedQuantity;
        private BigDecimal actualQuantity;
        private String unitOfMeasure;

        private Long productionCostSheetId;
        private String productionCostSheetCode;
        private String productionCostSheetName;
        private Integer productionCostSpecUnits;

        private BigDecimal plannedUnitCost;
        private BigDecimal plannedTotalCost;

        private String notes;
    }
}
