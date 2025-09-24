package ext.vnua.veterinary_beapp.modules.production.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionBatchRecord;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomProductionBatchRecordQuery {
    private CustomProductionBatchRecordQuery(){}

    @Data @NoArgsConstructor
    public static class ProductionBatchRecordFilterParam {
        private Long productionOrderId;
        private String stepName;
        private String approvedBy;
        private LocalDate fromRecordDate;
        private LocalDate toRecordDate;
        private String keywords; // stepName / result / approvedBy / orderCode
        private String sortField;
        private String sortType;
    }

    public static Specification<ProductionBatchRecord> getFilter(ProductionBatchRecordFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (p.productionOrderId != null) {
                Join<ProductionBatchRecord, ProductionOrder> oj = root.join("productionOrder");
                ps.add(cb.equal(oj.get("id"), p.productionOrderId));
            }
            if (p.stepName != null && !p.stepName.isBlank()) {
                ps.add(cb.like(cb.lower(root.get("stepName")), "%" + p.stepName.toLowerCase() + "%"));
            }
            if (p.approvedBy != null && !p.approvedBy.isBlank()) {
                ps.add(cb.like(cb.lower(root.get("approvedBy")), "%" + p.approvedBy.toLowerCase() + "%"));
            }
            if (p.fromRecordDate != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("recordDate"), p.fromRecordDate));
            }
            if (p.toRecordDate != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("recordDate"), p.toRecordDate));
            }
            if (p.keywords != null && !p.keywords.isBlank()) {
                var stepLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.keywords, "stepName");
                var resultLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.keywords, "result");
                var approvedLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.keywords, "approvedBy");
                Join<ProductionBatchRecord, ProductionOrder> oj = root.join("productionOrder");
                var orderCodeLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(oj, cb, p.keywords, "orderCode");
                ps.add(cb.or(stepLike, resultLike, approvedLike, orderCodeLike));
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
