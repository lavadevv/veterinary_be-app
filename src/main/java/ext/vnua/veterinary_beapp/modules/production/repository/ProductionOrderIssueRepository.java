package ext.vnua.veterinary_beapp.modules.production.repository;

import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrderIssue;
import ext.vnua.veterinary_beapp.modules.production.enums.IssueStatus;
import ext.vnua.veterinary_beapp.modules.production.enums.IssueType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductionOrderIssueRepository
        extends JpaRepository<ProductionOrderIssue, Long>, JpaSpecificationExecutor<ProductionOrderIssue> {

    Optional<ProductionOrderIssue> findByIssueCode(String issueCode);

    List<ProductionOrderIssue> findByProductionOrderId(Long orderId);


    List<ProductionOrderIssue> findByIssueType(IssueType type);


    boolean existsByProductionOrderIdAndIssueType(Long productionOrderId, IssueType issueType);
    List<ProductionOrderIssue> findByProductionOrderIdAndIssueType(Long productionOrderId, IssueType issueType);
    List<ProductionOrderIssue> findByStatus(IssueStatus status);
    boolean existsByProductionOrderIdAndStatus(Long productionOrderId, IssueStatus status);
    long countByProductionOrderIdAndIssueDateBetween(Long productionOrderId, LocalDate startDate, LocalDate endDate);
    boolean existsByIssueCode(String issueCode);

    @Query("SELECT i.status as status, COUNT(i) as count FROM ProductionOrderIssue i WHERE i.productionOrder.id = :orderId GROUP BY i.status")
    Map<IssueStatus, Long> countByProductionOrderIdGroupByStatus(@Param("orderId") Long orderId);

    @Query("SELECT i.status as status, COUNT(i) as count FROM ProductionOrderIssue i GROUP BY i.status")
    Map<IssueStatus, Long> countAllGroupByStatus();
}
