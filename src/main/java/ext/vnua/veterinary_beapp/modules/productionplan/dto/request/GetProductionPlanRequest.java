package ext.vnua.veterinary_beapp.modules.productionplan.dto.request;

import ext.vnua.veterinary_beapp.modules.productionplan.enums.ProductionPlanStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class GetProductionPlanRequest {

    private String lotNumber;
    private String keywords;

    private Long formulaId;

    private Long productId;

    private ProductionPlanStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
}
