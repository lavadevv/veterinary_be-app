package ext.vnua.veterinary_beapp.modules.production.dto.request;

import ext.vnua.veterinary_beapp.modules.production.enums.IssueType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProductionOrderIssueRequest {
    @NotNull
    private Long productionOrderId;

    @NotNull
    private IssueType issueType; // MATERIAL / PACKAGING

    private LocalDate issueDate;
    private String notes;
}
