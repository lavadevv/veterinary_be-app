// ext/vnua/veterinary_beapp/modules/material/repository/custom/CustomUnitOfMeasureQuery.java
package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.UnitOfMeasure;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomUnitOfMeasureQuery {

    private CustomUnitOfMeasureQuery(){}

    @Data
    @NoArgsConstructor
    public static class UomFilterParam {
        /** Tìm theo tên (LIKE, insensitive) */
        private String keywords;

        /** field để sort: "id" hoặc "name" */
        private String sortField;

        /** "ASC" | "DESC" | ""(mặc định ASC theo name nếu null/empty) */
        private String sortType;
    }

    public static Specification<UnitOfMeasure> getFilterUom(UomFilterParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (param.getKeywords() != null && !param.getKeywords().trim().isEmpty()) {
                predicates.add(CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.getKeywords().trim(), "name"));
            }

            // Sort
            String sortField = param.getSortField();
            String sortType = param.getSortType();
            if (sortField != null && !sortField.isEmpty()) {
                if (Constant.SortType.ASC.equalsIgnoreCase(sortType)) {
                    query.orderBy(cb.asc(root.get(sortField)));
                } else {
                    query.orderBy(cb.desc(root.get(sortField)));
                }
            } else {
                // mặc định sort theo name A→Z
                query.orderBy(cb.asc(root.get("name")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
