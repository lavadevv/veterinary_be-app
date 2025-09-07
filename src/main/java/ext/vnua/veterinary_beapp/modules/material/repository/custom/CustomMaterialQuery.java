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
        private Boolean lowStock; // Filter materials with current stock <= minimum stock level
        private Double minFixedPrice;
        private Double maxFixedPrice;
        private String sortField;
        private String sortType;
    }

    public static Specification<Material> getFilterMaterial(MaterialFilterParam param) {
        return ((root, query, criteriaBuilder) -> {
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
                        root, criteriaBuilder, param.keywords, "materialName");
                Predicate materialCodePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "materialCode");
                Predicate shortNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "shortName");

                Predicate supplierNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        supplierJoin, criteriaBuilder, param.keywords, "supplierName");

                predicates.add(criteriaBuilder.or(materialNamePredicate, materialCodePredicate,
                        shortNamePredicate, supplierNamePredicate));
            }

            if (param.materialType != null) {
                predicates.add(criteriaBuilder.equal(root.get("materialType"), param.materialType));
            }

            if (param.materialForm != null) {
                predicates.add(criteriaBuilder.equal(root.get("materialForm"), param.materialForm));
            }

            if (param.supplierId != null) {
                predicates.add(criteriaBuilder.equal(supplierJoin.get("id"), param.supplierId));
            }

            if (param.requiresColdStorage != null) {
                predicates.add(criteriaBuilder.equal(root.get("requiresColdStorage"), param.requiresColdStorage));
            }

            if (param.isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), param.isActive));
            }

            if (param.lowStock != null && param.lowStock) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("currentStock"), root.get("minimumStockLevel")));
            }

            // Filter by fixed price range
            if (param.minFixedPrice != null && param.maxFixedPrice != null) {
                predicates.add(criteriaBuilder.between(root.get("fixedPrice"), param.minFixedPrice, param.maxFixedPrice));
            } else if (param.minFixedPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fixedPrice"), param.minFixedPrice));
            } else if (param.maxFixedPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fixedPrice"), param.maxFixedPrice));
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
