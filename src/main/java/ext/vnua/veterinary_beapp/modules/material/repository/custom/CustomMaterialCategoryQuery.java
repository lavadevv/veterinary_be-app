// File: ext/vnua/veterinary_beapp/modules/material/repository/custom/CustomMaterialCategoryQuery.java
package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialCategory;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomMaterialCategoryQuery {

    private CustomMaterialCategoryQuery() {}

    @Data
    @NoArgsConstructor
    public static class CategoryFilterParam {
        /** Tìm theo tên loại (categoryName) */
        private String keywords;
        /** "id" | "categoryName" */
        private String sortField;
        /** "ASC" | "DESC" */
        private String sortType;
    }

    public static Specification<MaterialCategory> getFilter(MaterialCategoryQuery.CategoryFilterParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (param.getKeywords() != null && !param.getKeywords().trim().isEmpty()) {
                predicates.add(
                        CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                                root, cb, param.getKeywords().trim(), "categoryName"
                        )
                );
            }

            String sortField = param.getSortField();
            String sortType  = param.getSortType();
            boolean asc = Constant.SortType.ASC.equalsIgnoreCase(sortType);

            if (sortField != null && !sortField.isBlank()) {
                switch (sortField) {
                    case "id"           -> query.orderBy(asc ? cb.asc(root.get("id")) : cb.desc(root.get("id")));
                    case "categoryName" -> query.orderBy(asc ? cb.asc(root.get("categoryName")) : cb.desc(root.get("categoryName")));
                    default             -> query.orderBy(cb.asc(root.get("categoryName"))); // mặc định A→Z
                }
            } else {
                query.orderBy(cb.asc(root.get("categoryName"))); // mặc định
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // alias để tương thích tên ngắn
    public static class MaterialCategoryQuery extends CustomMaterialCategoryQuery {}
}
