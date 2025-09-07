package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomMaterialBatchQuery {

    private CustomMaterialBatchQuery() {}

    @Data
    @NoArgsConstructor
    public static class MaterialBatchFilterParam {
        private String keywords;
        private Long materialId;
        private Long locationId;
        private TestStatus testStatus;
        private UsageStatus usageStatus;
        private LocalDate receivedFromDate;
        private LocalDate receivedToDate;
        private LocalDate expiryFromDate;
        private LocalDate expiryToDate;
        private BigDecimal minQuantity;
        private BigDecimal maxQuantity;
        private Boolean nearExpiry; // Expiry within 30 days
        private Boolean expired; // Already expired
        private String sortField;
        private String sortType;
    }

    public static Specification<MaterialBatch> getFilterMaterialBatch(MaterialBatchFilterParam param) {
        return ((root, query, criteriaBuilder) -> {
            root.fetch("material", JoinType.LEFT)
                    .fetch("supplier", JoinType.LEFT);
            root.fetch("location", JoinType.LEFT);

            if (Long.class == query.getResultType()) {
                root.join("material", JoinType.LEFT).join("supplier", JoinType.LEFT);
                root.join("location", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (param.keywords != null && !param.keywords.trim().isEmpty()) {
                Predicate batchNumberPredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "batchNumber");
                Predicate internalBatchCodePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "internalBatchCode");
                Predicate manufacturerBatchPredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "manufacturerBatchNumber");

                // Search in material name as well
                Join<MaterialBatch, Material> materialJoin = root.join("material");
                Predicate materialNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        materialJoin, criteriaBuilder, param.keywords, "materialName");

                predicates.add(criteriaBuilder.or(batchNumberPredicate, internalBatchCodePredicate,
                        manufacturerBatchPredicate, materialNamePredicate));
            }

            if (param.materialId != null) {
                Join<MaterialBatch, Material> materialJoin = root.join("material");
                predicates.add(criteriaBuilder.equal(materialJoin.get("id"), param.materialId));
            }

            if (param.locationId != null) {
                Join<MaterialBatch, Location> locationJoin = root.join("location");
                predicates.add(criteriaBuilder.equal(locationJoin.get("id"), param.locationId));
            }

            if (param.testStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("testStatus"), param.testStatus));
            }

            if (param.usageStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("usageStatus"), param.usageStatus));
            }

            // Filter by received date range
            if (param.receivedFromDate != null && param.receivedToDate != null) {
                predicates.add(criteriaBuilder.between(root.get("receivedDate"),
                        param.receivedFromDate, param.receivedToDate));
            } else if (param.receivedFromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("receivedDate"), param.receivedFromDate));
            } else if (param.receivedToDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("receivedDate"), param.receivedToDate));
            }

            // Filter by expiry date range
            if (param.expiryFromDate != null && param.expiryToDate != null) {
                predicates.add(criteriaBuilder.between(root.get("expiryDate"),
                        param.expiryFromDate, param.expiryToDate));
            } else if (param.expiryFromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("expiryDate"), param.expiryFromDate));
            } else if (param.expiryToDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("expiryDate"), param.expiryToDate));
            }

            // Filter by quantity range
            if (param.minQuantity != null && param.maxQuantity != null) {
                predicates.add(criteriaBuilder.between(root.get("currentQuantity"), param.minQuantity, param.maxQuantity));
            } else if (param.minQuantity != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("currentQuantity"), param.minQuantity));
            } else if (param.maxQuantity != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("currentQuantity"), param.maxQuantity));
            }

            // Filter near expiry (within 30 days)
            if (param.nearExpiry != null && param.nearExpiry) {
                LocalDate today = LocalDate.now();
                LocalDate thirtyDaysFromNow = today.plusDays(30);
                predicates.add(criteriaBuilder.between(root.get("expiryDate"), today, thirtyDaysFromNow));
            }

            // Filter expired
            if (param.expired != null && param.expired) {
                predicates.add(criteriaBuilder.lessThan(root.get("expiryDate"), LocalDate.now()));
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
