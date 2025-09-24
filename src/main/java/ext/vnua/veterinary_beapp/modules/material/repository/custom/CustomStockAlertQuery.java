package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.StockAlert;
import ext.vnua.veterinary_beapp.modules.material.model.StockAlert.AlertType;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomStockAlertQuery {

    private CustomStockAlertQuery() {}

    @Data
    @NoArgsConstructor
    public static class StockAlertFilterParam {
        private String keywords;
        private Long materialId;
        private AlertType alertType;
        private Boolean isResolved;
        private Long resolvedById;
        private LocalDateTime alertFromDate;
        private LocalDateTime alertToDate;
        private LocalDateTime resolvedFromDate;
        private LocalDateTime resolvedToDate;
        private String sortField;
        private String sortType;
    }

    public static Specification<StockAlert> getFilterStockAlert(StockAlertFilterParam param) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by keywords in alertMessage and materialName
            if (param.keywords != null && !param.keywords.trim().isEmpty()) {
                Predicate alertMessagePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "alertMessage");

                Join<StockAlert, Material> materialJoin = root.join("material");
                Predicate materialNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        materialJoin, criteriaBuilder, param.keywords, "materialName");

                predicates.add(criteriaBuilder.or(alertMessagePredicate, materialNamePredicate));
            }

            // Filter by materialId
            if (param.materialId != null) {
                Join<StockAlert, Material> materialJoin = root.join("material");
                predicates.add(criteriaBuilder.equal(materialJoin.get("id"), param.materialId));
            }

            // Filter by alertType
            if (param.alertType != null) {
                predicates.add(criteriaBuilder.equal(root.get("alertType"), param.alertType));
            }

            // Filter by isResolved
            if (param.isResolved != null) {
                predicates.add(criteriaBuilder.equal(root.get("isResolved"), param.isResolved));
            }

            // Filter by resolvedById
            if (param.resolvedById != null) {
                Join<StockAlert, User> resolvedByJoin = root.join("resolvedBy");
                predicates.add(criteriaBuilder.equal(resolvedByJoin.get("id"), param.resolvedById));
            }

            // Filter by alert date range
            if (param.alertFromDate != null && param.alertToDate != null) {
                predicates.add(criteriaBuilder.between(root.get("alertDate"), param.alertFromDate, param.alertToDate));
            } else if (param.alertFromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("alertDate"), param.alertFromDate));
            } else if (param.alertToDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("alertDate"), param.alertToDate));
            }

            // Filter by resolved date range
            if (param.resolvedFromDate != null && param.resolvedToDate != null) {
                predicates.add(criteriaBuilder.between(root.get("resolvedDate"), param.resolvedFromDate, param.resolvedToDate));
            } else if (param.resolvedFromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("resolvedDate"), param.resolvedFromDate));
            } else if (param.resolvedToDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("resolvedDate"), param.resolvedToDate));
            }

            // Sorting
            if (param.sortField != null && !param.sortField.equals("")) {
                if (param.sortType != null && param.sortType.equals(Constant.SortType.ASC)) {
                    query.orderBy(criteriaBuilder.asc(root.get(param.sortField)));
                } else {
                    query.orderBy(criteriaBuilder.desc(root.get(param.sortField)));
                }
            } else {
                query.orderBy(criteriaBuilder.desc(root.get("id")));

            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }

}