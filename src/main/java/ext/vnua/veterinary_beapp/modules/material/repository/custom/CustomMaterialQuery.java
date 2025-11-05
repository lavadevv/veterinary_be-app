// File: ext/vnua/veterinary_beapp/modules/material/repository/custom/CustomMaterialQuery.java
package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialCategory;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialFormType;
import ext.vnua.veterinary_beapp.modules.material.model.Supplier;
import ext.vnua.veterinary_beapp.modules.material.model.UnitOfMeasure;
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

        private Long materialCategoryId;
        private Long materialFormTypeId;

        private Long supplierId;
        private Boolean requiresColdStorage;
        private Boolean isActive;
        private Boolean lowStock; // currentStock <= minimumStockLevel

        private BigDecimal minFixedPrice;
        private BigDecimal maxFixedPrice;

        // id | materialName | materialCode | supplierName | categoryName | formTypeName
        // | fixedPrice | currentStock | minimumStockLevel
        private String sortField;
        private String sortType; // ASC | DESC
    }

    public static Specification<Material> getFilterMaterial(MaterialFilterParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ===== JOINs =====
            Join<Material, Supplier> supplierJoin = root.join("supplier", JoinType.LEFT);
            Join<Material, MaterialCategory> catJoin = root.join("materialCategory", JoinType.LEFT);
            Join<Material, MaterialFormType> formJoin = root.join("materialFormType", JoinType.LEFT);
            Join<Material, UnitOfMeasure> uomJoin = root.join("unitOfMeasure", JoinType.LEFT);

            // ===== FETCH (tránh lazy) cho non-count =====
            Class<?> resultType = query.getResultType();
            boolean isCount = Long.class.equals(resultType) || long.class.equals(resultType);
            if (!isCount) {
                root.fetch("supplier", JoinType.LEFT);
                root.fetch("materialCategory", JoinType.LEFT);
                root.fetch("materialFormType", JoinType.LEFT);
                root.fetch("unitOfMeasure", JoinType.LEFT);
                query.distinct(true);
            }

            // ===== Keywords =====
            if (param.getKeywords() != null && !param.getKeywords().trim().isEmpty()) {
                String kw = param.getKeywords().trim();

                Predicate pMatName = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, kw, "materialName");
                Predicate pMatCode = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, kw, "materialCode");
                Predicate pSupp    = CriteriaBuilderUtil.createPredicateForSearchInsensitive(supplierJoin, cb, kw, "supplierName");
                Predicate pCat     = CriteriaBuilderUtil.createPredicateForSearchInsensitive(catJoin, cb, kw, "categoryName");
                Predicate pForm    = CriteriaBuilderUtil.createPredicateForSearchInsensitive(formJoin, cb, kw, "name");
                Predicate pUomN    = CriteriaBuilderUtil.createPredicateForSearchInsensitive(uomJoin, cb, kw, "name");

                predicates.add(cb.or(pMatName, pMatCode, pSupp, pCat, pForm, pUomN));
            }

            // ===== Filters =====
            if (param.getMaterialCategoryId() != null) {
                predicates.add(cb.equal(catJoin.get("id"), param.getMaterialCategoryId()));
            }
            if (param.getMaterialFormTypeId() != null) {
                predicates.add(cb.equal(formJoin.get("id"), param.getMaterialFormTypeId()));
            }
            if (param.getSupplierId() != null) {
                predicates.add(cb.equal(supplierJoin.get("id"), param.getSupplierId()));
            }
            if (param.getRequiresColdStorage() != null) {
                predicates.add(cb.equal(root.get("requiresColdStorage"), param.getRequiresColdStorage()));
            }
            if (param.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), param.getIsActive()));
            }
            if (Boolean.TRUE.equals(param.getLowStock())) {
                predicates.add(cb.lessThanOrEqualTo(root.get("currentStock"), root.get("minimumStockLevel")));
            }

            // fixed price range
            if (param.getMinFixedPrice() != null && param.getMaxFixedPrice() != null) {
                predicates.add(cb.between(root.get("fixedPrice"), param.getMinFixedPrice(), param.getMaxFixedPrice()));
            } else if (param.getMinFixedPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fixedPrice"), param.getMinFixedPrice()));
            } else if (param.getMaxFixedPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fixedPrice"), param.getMaxFixedPrice()));
            }

            // ===== Sorting =====
            String sf = param.getSortField();
            String st = param.getSortType();
            boolean asc = Constant.SortType.ASC.equalsIgnoreCase(st);

            if (sf != null && !sf.isBlank()) {
                switch (sf) {
                    case "id" -> query.orderBy(asc ? cb.asc(root.get("id")) : cb.desc(root.get("id")));
                    case "materialName" -> query.orderBy(asc ? cb.asc(root.get("materialName")) : cb.desc(root.get("materialName")));
                    case "materialCode" -> query.orderBy(asc ? cb.asc(root.get("materialCode")) : cb.desc(root.get("materialCode")));
                    case "supplierName" -> query.orderBy(asc ? cb.asc(supplierJoin.get("supplierName")) : cb.desc(supplierJoin.get("supplierName")));
                    case "categoryName" -> query.orderBy(asc ? cb.asc(catJoin.get("categoryName")) : cb.desc(catJoin.get("categoryName")));
                    case "formTypeName" -> query.orderBy(asc ? cb.asc(formJoin.get("name")) : cb.desc(formJoin.get("name")));
                    case "fixedPrice" -> query.orderBy(asc ? cb.asc(root.get("fixedPrice")) : cb.desc(root.get("fixedPrice")));
                    case "currentStock" -> query.orderBy(asc ? cb.asc(root.get("currentStock")) : cb.desc(root.get("currentStock")));
                    case "minimumStockLevel" -> query.orderBy(asc ? cb.asc(root.get("minimumStockLevel")) : cb.desc(root.get("minimumStockLevel")));
                    default -> query.orderBy(cb.desc(root.get("id")));
                }
            } else {
                query.orderBy(cb.desc(root.get("id")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
