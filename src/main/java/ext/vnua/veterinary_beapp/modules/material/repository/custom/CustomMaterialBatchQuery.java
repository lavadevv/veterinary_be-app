package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import jakarta.persistence.criteria.FetchParent;
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
        return (root, query, cb) -> {
            // TODO: This query needs major refactoring for MaterialBatchItem structure
            // Currently simplified to work with new MaterialBatch container pattern
            
            // Phân biệt query đếm (count) hay query dữ liệu
            boolean isCountQuery =
                    Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType());

            // Chỉ fetch khi KHÔNG phải count
            if (!isCountQuery) {
                // Fetch batchItems thay vì material trực tiếp
                root.fetch("batchItems", JoinType.LEFT);
                root.fetch("location", JoinType.LEFT).fetch("warehouse", JoinType.LEFT);
                root.fetch("supplier", JoinType.LEFT);
                root.fetch("manufacturer", JoinType.LEFT);
                query.distinct(true); // tránh trùng dòng do join
            }

            List<Predicate> predicates = new ArrayList<>();

            // ----- Tìm kiếm theo keywords -----
            if (param.keywords != null && !param.keywords.trim().isEmpty()) {
                Predicate byBatchNo = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.keywords, "batchNumber");
                Predicate byInternal = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.keywords, "internalBatchCode");

                // TODO: Search by material name needs to join through batchItems
                predicates.add(cb.or(byBatchNo, byInternal));
            }

            // ----- Filter theo materialId -----
            // TODO: Needs to filter through batchItems.material
            // if (param.materialId != null) {
            //     Join to batchItems then material
            // }

            // ----- Filter theo locationId -----
            if (param.locationId != null) {
                Join<MaterialBatch, Location> l = root.join("location", JoinType.LEFT);
                predicates.add(cb.equal(l.get("id"), param.locationId));
            }

            // TODO: testStatus, usageStatus are now on MaterialBatchItem, not MaterialBatch
            // if (param.testStatus != null) {
            //     predicates.add(cb.equal(root.get("testStatus"), param.testStatus));
            // }
            // if (param.usageStatus != null) {
            //     predicates.add(cb.equal(root.get("usageStatus"), param.usageStatus));
            // }

            // ----- Ngày nhập -----
            if (param.receivedFromDate != null && param.receivedToDate != null) {
                predicates.add(cb.between(root.get("receivedDate"), param.receivedFromDate, param.receivedToDate));
            } else if (param.receivedFromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("receivedDate"), param.receivedFromDate));
            } else if (param.receivedToDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("receivedDate"), param.receivedToDate));
            }

            // TODO: expiryDate is now on MaterialBatchItem
            // TODO: currentQuantity is now on MaterialBatchItem
            // TODO: nearExpiry and expired filters need to query MaterialBatchItem

            // ----- Sort -----
            if (param.sortField != null && !param.sortField.isBlank()) {
                if (Constant.SortType.ASC.equals(param.sortType)) {
                    query.orderBy(cb.asc(root.get(param.sortField)));
                } else {
                    query.orderBy(cb.desc(root.get(param.sortField)));
                }
            } else {
                query.orderBy(cb.desc(root.get("id")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
