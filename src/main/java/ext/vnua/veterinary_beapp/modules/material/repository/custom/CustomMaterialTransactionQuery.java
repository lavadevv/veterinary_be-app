package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialTransaction;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialTransaction.TransactionType;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomMaterialTransactionQuery {

    private CustomMaterialTransactionQuery() {}

    @Data
    @NoArgsConstructor
    public static class MaterialTransactionFilterParam {
        private String keywords;
        private Long materialId;
        private Long materialBatchId;
        private TransactionType transactionType;
        private Long createdById;
        private Long approvedById;
        private LocalDateTime transactionFromDate;
        private LocalDateTime transactionToDate;
        private BigDecimal minQuantity;
        private BigDecimal maxQuantity;
        private String productionOrderId;
        private String sortField;
        private String sortType;
    }

    public static Specification<MaterialTransaction> getFilterMaterialTransaction(MaterialTransactionFilterParam param) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (param.keywords != null && !param.keywords.trim().isEmpty()) {
                Predicate referenceDocPredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "referenceDocument");
                Predicate productionOrderPredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "productionOrderId");

                // Search in material name
                Join<MaterialTransaction, MaterialBatch> batchJoin = root.join("materialBatch");
                Join<MaterialBatch, Material> materialJoin = batchJoin.join("material");
                Predicate materialNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        materialJoin, criteriaBuilder, param.keywords, "materialName");

                predicates.add(criteriaBuilder.or(referenceDocPredicate, productionOrderPredicate, materialNamePredicate));
            }

            if (param.materialBatchId != null) {
                Join<MaterialTransaction, MaterialBatch> batchJoin = root.join("materialBatch");
                predicates.add(criteriaBuilder.equal(batchJoin.get("id"), param.materialBatchId));
            }

            if (param.materialId != null) {
                Join<MaterialTransaction, MaterialBatch> batchJoin = root.join("materialBatch");
                Join<MaterialBatch, Material> materialJoin = batchJoin.join("material");
                predicates.add(criteriaBuilder.equal(materialJoin.get("id"), param.materialId));
            }

            if (param.transactionType != null) {
                predicates.add(criteriaBuilder.equal(root.get("transactionType"), param.transactionType));
            }

            if (param.createdById != null) {
                Join<MaterialTransaction, User> createdByJoin = root.join("createdBy");
                predicates.add(criteriaBuilder.equal(createdByJoin.get("id"), param.createdById));
            }

            if (param.approvedById != null) {
                Join<MaterialTransaction, User> approvedByJoin = root.join("approvedBy");
                predicates.add(criteriaBuilder.equal(approvedByJoin.get("id"), param.approvedById));
            }

            if (param.productionOrderId != null && !param.productionOrderId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("productionOrderId"), param.productionOrderId));
            }

            // Filter by transaction date range
            if (param.transactionFromDate != null && param.transactionToDate != null) {
                predicates.add(criteriaBuilder.between(root.get("transactionDate"),
                        param.transactionFromDate, param.transactionToDate));
            } else if (param.transactionFromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), param.transactionFromDate));
            } else if (param.transactionToDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), param.transactionToDate));
            }

            // Filter by quantity range
            if (param.minQuantity != null && param.maxQuantity != null) {
                predicates.add(criteriaBuilder.between(root.get("quantity"), param.minQuantity, param.maxQuantity));
            } else if (param.minQuantity != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("quantity"), param.minQuantity));
            } else if (param.maxQuantity != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("quantity"), param.maxQuantity));
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
