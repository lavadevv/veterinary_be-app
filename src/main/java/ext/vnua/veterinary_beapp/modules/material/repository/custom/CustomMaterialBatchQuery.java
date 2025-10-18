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
        return (root, query, cb) -> {
            // Phân biệt query đếm (count) hay query dữ liệu
            boolean isCountQuery =
                    Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType());

            // Chỉ fetch khi KHÔNG phải count
            if (!isCountQuery) {
                root.fetch("material", JoinType.LEFT).fetch("supplier", JoinType.LEFT);
                root.fetch("location", JoinType.LEFT);
                query.distinct(true); // tránh trùng dòng do join
            } else {
                // CountQuery: chỉ join thường, tuyệt đối không fetch
                root.join("material", JoinType.LEFT).join("supplier", JoinType.LEFT);
                root.join("location", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            // ----- Tìm kiếm theo keywords -----
            if (param.keywords != null && !param.keywords.trim().isEmpty()) {
                // join tạm cho điều kiện (an toàn cả 2 nhánh)
                Join<MaterialBatch, Material> m = root.join("material", JoinType.LEFT);

                Predicate byBatchNo = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.keywords, "batchNumber");
                Predicate byInternal = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.keywords, "internalBatchCode");
                Predicate byMfg = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.keywords, "manufacturerBatchNumber");
                Predicate byMatName = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        m, cb, param.keywords, "materialName");

                predicates.add(cb.or(byBatchNo, byInternal, byMfg, byMatName));
            }

            // ----- Filter theo materialId -----
            if (param.materialId != null) {
                Join<MaterialBatch, Material> m = root.join("material", JoinType.LEFT);
                predicates.add(cb.equal(m.get("id"), param.materialId));
            }

            // ----- Filter theo locationId -----
            if (param.locationId != null) {
                Join<MaterialBatch, Location> l = root.join("location", JoinType.LEFT);
                predicates.add(cb.equal(l.get("id"), param.locationId));
            }

            if (param.testStatus != null) {
                predicates.add(cb.equal(root.get("testStatus"), param.testStatus));
            }
            if (param.usageStatus != null) {
                predicates.add(cb.equal(root.get("usageStatus"), param.usageStatus));
            }

            // ----- Ngày nhập -----
            if (param.receivedFromDate != null && param.receivedToDate != null) {
                predicates.add(cb.between(root.get("receivedDate"), param.receivedFromDate, param.receivedToDate));
            } else if (param.receivedFromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("receivedDate"), param.receivedFromDate));
            } else if (param.receivedToDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("receivedDate"), param.receivedToDate));
            }

            // ----- Hạn dùng -----
            if (param.expiryFromDate != null && param.expiryToDate != null) {
                predicates.add(cb.between(root.get("expiryDate"), param.expiryFromDate, param.expiryToDate));
            } else if (param.expiryFromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expiryDate"), param.expiryFromDate));
            } else if (param.expiryToDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expiryDate"), param.expiryToDate));
            }

            // ----- Số lượng -----
            if (param.minQuantity != null && param.maxQuantity != null) {
                predicates.add(cb.between(root.get("currentQuantity"), param.minQuantity, param.maxQuantity));
            } else if (param.minQuantity != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("currentQuantity"), param.minQuantity));
            } else if (param.maxQuantity != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("currentQuantity"), param.maxQuantity));
            }

            // ----- Gần hết hạn (30 ngày) -----
            if (param.nearExpiry != null && param.nearExpiry) {
                LocalDate today = LocalDate.now();
                LocalDate thirty = today.plusDays(30);
                predicates.add(cb.between(root.get("expiryDate"), today, thirty));
            }

            // ----- Đã hết hạn -----
            if (param.expired != null && param.expired) {
                predicates.add(cb.lessThan(root.get("expiryDate"), LocalDate.now()));
            }

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
