package ext.vnua.veterinary_beapp.modules.product.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBrand;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomProductBrandQuery {

    private CustomProductBrandQuery() {}

    @Data
    @NoArgsConstructor
    public static class ProductBrandFilterParam {
        private String keywords; // Tìm theo product code, product name, brand name
        private Long productId;
        private Long brandId;
        private Long productionCostSheetId;
        private Boolean isActive;
        private String sortField;
        private String sortType;
    }

    public static Specification<ProductBrand> getFilterProductBrand(ProductBrandFilterParam param) {
        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by keywords (product code, product name, brand name)
            if (param.keywords != null && !param.keywords.isEmpty()) {
                Predicate productCodePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                    root.join("product"), cb, param.keywords, "productCode");
                Predicate productNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                    root.join("product"), cb, param.keywords, "productName");
                Predicate brandNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                    root.join("brand"), cb, param.keywords, "name");
                predicates.add(cb.or(productCodePredicate, productNamePredicate, brandNamePredicate));
            }

            // Filter by productId
            if (param.productId != null) {
                predicates.add(cb.equal(root.get("product").get("id"), param.productId));
            }

            // Filter by brandId
            if (param.brandId != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), param.brandId));
            }

            // Filter by productionCostSheetId
            if (param.productionCostSheetId != null) {
                predicates.add(cb.equal(root.get("productionCostSheet").get("id"), param.productionCostSheetId));
            }

            // Filter by isActive
            if (param.isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), param.isActive));
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
        });
    }
}
