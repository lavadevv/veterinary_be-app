package ext.vnua.veterinary_beapp.modules.productionplan.dto.request;

import ext.vnua.veterinary_beapp.modules.productionplan.enums.ProductionPlanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProductionPlanListRow {
    private Long id;

    private String lotNumber;
    private LocalDate planDate;
    private Integer planMonth;
    private Integer planYear;
    private BigDecimal batchSize;
    private ProductionPlanStatus status;

    private Long formulaId;
    private String formulaCode;
    private String formulaName;
    private String formulaVersion;

    private Long productLines;          // number of products in the plan
    private BigDecimal totalPlannedQty; // sum of planned quantities

    private LocalDateTime createdDate;
    private String createdBy;
}

