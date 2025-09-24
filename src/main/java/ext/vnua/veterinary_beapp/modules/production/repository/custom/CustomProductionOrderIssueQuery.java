package ext.vnua.veterinary_beapp.modules.production.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.production.enums.IssueType;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrderIssue;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomProductionOrderIssueQuery {
    private CustomProductionOrderIssueQuery(){}

    @Data @NoArgsConstructor
    public static class ProductionOrderIssueFilterParam {
        private Long productionOrderId;
        private IssueType issueType; // MATERIAL / PACKAGING
        private String status; // PENDING / COMPLETED / CANCELLED (String trong model)
        private LocalDate fromIssueDate;
        private LocalDate toIssueDate;
        private String keywords; // orderCode / notes
        private String sortField;
        private String sortType;
    }

    public static Specification<ProductionOrderIssue> getFilter(ProductionOrderIssueFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (p.productionOrderId != null) {
                Join<ProductionOrderIssue, ProductionOrder> oj = root.join("productionOrder");
                ps.add(cb.equal(oj.get("id"), p.productionOrderId));
            }
            if (p.issueType != null) {
                ps.add(cb.equal(root.get("issueType"), p.issueType));
            }
            if (p.status != null && !p.status.isBlank()) {
                ps.add(cb.equal(root.get("status"), p.status));
            }
            if (p.fromIssueDate != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("issueDate"), p.fromIssueDate));
            }
            if (p.toIssueDate != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("issueDate"), p.toIssueDate));
            }
            if (p.keywords != null && !p.keywords.isBlank()) {
                Join<ProductionOrderIssue, ProductionOrder> oj = root.join("productionOrder");
                var orderCodeLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(oj, cb, p.keywords, "orderCode");
                var notesLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.keywords, "notes");
                ps.add(cb.or(orderCodeLike, notesLike));
            }

            if (p.sortField != null && !p.sortField.isBlank()) {
                if (Constant.SortType.ASC.equals(p.sortType)) query.orderBy(cb.asc(root.get(p.sortField)));
                else query.orderBy(cb.desc(root.get(p.sortField)));
            } else {
                query.orderBy(cb.desc(root.get("id")));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
