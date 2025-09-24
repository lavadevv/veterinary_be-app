package ext.vnua.veterinary_beapp.modules.production.services;

import ext.vnua.veterinary_beapp.modules.production.dto.ProductionOrderIssueDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionOrderIssue.CreateProductionOrderIssueRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionOrderIssue.UpdateProductionOrderIssueRequest;
import ext.vnua.veterinary_beapp.modules.production.enums.IssueStatus;
import ext.vnua.veterinary_beapp.modules.production.enums.IssueType;
import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionOrderIssueQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

public interface ProductionOrderIssueService {

    Page<ProductionOrderIssueDto> getAll(CustomProductionOrderIssueQuery.ProductionOrderIssueFilterParam param, PageRequest pr);

    ProductionOrderIssueDto getById(Long id);

    ProductionOrderIssueDto getByCode(String issueCode);

    List<ProductionOrderIssueDto> getByOrder(Long orderId);

    ProductionOrderIssueDto create(CreateProductionOrderIssueRequest req);

    ProductionOrderIssueDto update(UpdateProductionOrderIssueRequest req);

    void delete(Long id);

    List<ProductionOrderIssueDto> deleteAll(List<Long> ids);

    // New methods to add to the interface
    List<ProductionOrderIssueDto> getByOrderAndType(Long orderId, IssueType issueType);
    List<ProductionOrderIssueDto> getPendingIssues();
    ProductionOrderIssueDto approve(Long issueId, Long approverId);
    ProductionOrderIssueDto cancel(Long issueId, String reason);
    Map<IssueStatus, Long> getStatusStatistics(Long orderId);
    boolean hasActiveIssues(Long orderId);
}
