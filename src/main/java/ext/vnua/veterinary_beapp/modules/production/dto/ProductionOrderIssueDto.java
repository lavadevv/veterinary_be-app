package ext.vnua.veterinary_beapp.modules.production.dto;

import ext.vnua.veterinary_beapp.modules.production.enums.IssueStatus;
import ext.vnua.veterinary_beapp.modules.production.enums.IssueType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductionOrderIssueDto {
    private Long id;
    private Long productionOrderId;
    private String orderCode;
    private String issueCode;
    private LocalDate issueDate;
    private IssueType issueType;
    private IssueStatus status;
    private String notes;
    private String approvedBy; // fullName hoặc email người duyệt
}
