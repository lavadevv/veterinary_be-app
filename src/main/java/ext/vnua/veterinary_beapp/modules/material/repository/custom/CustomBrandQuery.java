package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.Brand;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomBrandQuery {

    private CustomBrandQuery() {}

    @Data
    @NoArgsConstructor
    public static class BrandFilterParam {
        private String keywords;
        /** ví dụ: "id", "name", "createdDate", ... */
        private String sortField;
        /** "ASC" | "DESC" */
        private String sortType;
    }

    public static Specification<Brand> getFilterBrand(BrandFilterParam param) {
        return (root, query, cb) -> {
            // ========= DISTINCT để tránh trùng lặp =========
            Class<?> resultType = query.getResultType();
            boolean isCountQuery = Long.class.equals(resultType) || long.class.equals(resultType);
            if (!isCountQuery) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            // ====== Keyword search ======
            if (param.getKeywords() != null && !param.getKeywords().trim().isEmpty()) {
                String kw = param.getKeywords().trim();

                // Search in brand name (case insensitive with unaccent)
                Predicate pBrandName = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, kw, "name");

                predicates.add(pBrandName);
            }

            // ====== Sorting ======
            String sortField = param.getSortField();
            String sortType = param.getSortType();
            if (sortField != null && !sortField.isBlank()) {
                boolean asc = Constant.SortType.ASC.equalsIgnoreCase(sortType);
                query.orderBy(asc ? cb.asc(root.get(sortField)) : cb.desc(root.get(sortField)));
            } else {
                // Default: sort by id DESC
                query.orderBy(cb.desc(root.get("id")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
