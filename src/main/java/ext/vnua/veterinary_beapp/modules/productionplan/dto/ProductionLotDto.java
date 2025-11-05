package ext.vnua.veterinary_beapp.modules.productionplan.dto;

import ext.vnua.veterinary_beapp.modules.productionplan.enums.ProductionPlanStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductionLotDto {
    private Long id;
    private String lotNumber;
    private Integer sequenceInMonth;
    private Integer planMonth;
    private Integer planYear;
    private LocalDate planDate;
    private ProductionPlanStatus status;
    private String notes;

    private Integer planCount;
    private BigDecimal totalPlannedQty;

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;

    private List<FormulaBrief> formulas = new ArrayList<>();

    @Data
    public static class FormulaBrief {
        private Long formulaId;
        private String formulaCode;
        private String formulaName;
        private String version;
    }
}

