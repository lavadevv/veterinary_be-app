package ext.vnua.veterinary_beapp.modules.production.dto.request.productionOrderIssue;

import ext.vnua.veterinary_beapp.modules.production.enums.IssueType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProductionOrderIssueRequest {

    @NotNull(message = "ID lệnh sản xuất không được để trống")
    private Long productionOrderId;

    @NotNull(message = "Loại phiếu cấp phát không được để trống")
    private IssueType issueType;

    @NotNull(message = "Ngày cấp phát không được để trống")
    private LocalDate issueDate;

    private String notes;
}
