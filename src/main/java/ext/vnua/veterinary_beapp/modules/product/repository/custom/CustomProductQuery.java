package ext.vnua.veterinary_beapp.modules.product.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.product.enums.ProductCategory;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomProductQuery {

    private CustomProductQuery() {}

    @Data
    @NoArgsConstructor
    public static class ProductFilterParam {
        private String keywords;
        private Boolean isActive;
        private String category;
        private String sortField;
        private String sortType;
    }

    public static Specification<Product> getFilterProduct(ProductFilterParam param) {
        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (param.keywords != null && !param.keywords.isEmpty()) {
                Predicate codePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, param.keywords, "productCode");
                Predicate namePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, param.keywords, "productName");
                predicates.add(cb.or(codePredicate, namePredicate));
            }

            if (param.isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), param.isActive));
            }

            if (param.category != null) {
                try {
                    ProductCategory categoryEnum = ProductCategory.valueOf(param.category);
                    predicates.add(cb.equal(root.get("productCategory"), categoryEnum));
                } catch (IllegalArgumentException e) {
                    // có thể log cảnh báo hoặc bỏ qua
                }
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
