package ext.vnua.veterinary_beapp.modules.production.dto.request.productionOrderIssue;

import ext.vnua.veterinary_beapp.modules.production.enums.IssueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProductionOrderIssueRequest {

    @NotNull(message = "ID phiếu cấp phát không được để trống")
    private Long id;

    private LocalDate issueDate;
    private IssueStatus status;
    private String notes;
}
