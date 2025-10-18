package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.enums.MaterialForm;
import ext.vnua.veterinary_beapp.modules.material.enums.MaterialType;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.Supplier;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CustomMaterialQuery {

    private CustomMaterialQuery() {}

    @Data
    @NoArgsConstructor
    public static class MaterialFilterParam {
        private String keywords;
        private MaterialType materialType;
        private MaterialForm materialForm;
        private Long supplierId;
        private Boolean requiresColdStorage;
        private Boolean isActive;
        private Boolean lowStock; // currentStock <= minimumStockLevel
        private BigDecimal minFixedPrice; // ĐỔI sang BigDecimal
        private BigDecimal maxFixedPrice; // ĐỔI sang BigDecimal
        private String sortField;
        private String sortType;
    }

    public static Specification<Material> getFilterMaterial(MaterialFilterParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Luôn join supplier để dùng filter
            Join<Material, Supplier> supplierJoin = root.join("supplier", JoinType.LEFT);

            // Fetch supplier khi không phải count query
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("supplier", JoinType.LEFT);
                query.distinct(true);
            }

            if (param.keywords != null && !param.keywords.trim().isEmpty()) {
                Predicate materialNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.keywords, "materialName");
                Predicate materialCodePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.keywords, "materialCode");
                Predicate shortNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.keywords, "shortName");
                Predicate supplierNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        supplierJoin, cb, param.keywords, "supplierName");

                predicates.add(cb.or(materialNamePredicate, materialCodePredicate, shortNamePredicate, supplierNamePredicate));
            }

            if (param.materialType != null) {
                predicates.add(cb.equal(root.get("materialType"), param.materialType));
            }

            if (param.materialForm != null) {
                predicates.add(cb.equal(root.get("materialForm"), param.materialForm));
            }

            if (param.supplierId != null) {
                predicates.add(cb.equal(supplierJoin.get("id"), param.supplierId));
            }

            if (param.requiresColdStorage != null) {
                predicates.add(cb.equal(root.get("requiresColdStorage"), param.requiresColdStorage));
            }

            if (param.isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), param.isActive));
            }

            if (param.lowStock != null && param.lowStock) {
                predicates.add(cb.lessThanOrEqualTo(root.get("currentStock"), root.get("minimumStockLevel")));
            }

            // Filter by fixed price range (BigDecimal)
            if (param.minFixedPrice != null && param.maxFixedPrice != null) {
                predicates.add(cb.between(root.get("fixedPrice"), param.minFixedPrice, param.maxFixedPrice));
            } else if (param.minFixedPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fixedPrice"), param.minFixedPrice));
            } else if (param.maxFixedPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fixedPrice"), param.maxFixedPrice));
            }

            // Sorting
            if (param.sortField != null && !param.sortField.isEmpty()) {
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
