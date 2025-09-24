package ext.vnua.veterinary_beapp.modules.production.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProductionBatchRecordRequest {
    @NotNull
    private Long productionOrderId;

    @NotNull
    private LocalDate recordDate;

    @NotBlank
    private String stepName;

    private String result;
    private String approvedBy;
}
