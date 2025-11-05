// File: ext/vnua/veterinary_beapp/modules/material/repository/custom/CustomMaterialFormTypeQuery.java
package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialFormType;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomMaterialFormTypeQuery {

    private CustomMaterialFormTypeQuery(){}

    @Data
    @NoArgsConstructor
    public static class FilterParam {
        private String keywords;   // search theo form_name
        private String sortField;  // id | name | createdAt
        private String sortType;   // ASC | DESC
    }

    public static Specification<MaterialFormType> getFilter(FilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            if (p.getKeywords() != null && !p.getKeywords().trim().isEmpty()) {
                preds.add(CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, p.getKeywords().trim(), "name"));
            }

            boolean asc = Constant.SortType.ASC.equalsIgnoreCase(p.getSortType());
            String sf = p.getSortField();
            if (sf != null && !sf.isBlank()) {
                switch (sf) {
                    case "id"        -> query.orderBy(asc ? cb.asc(root.get("id")) : cb.desc(root.get("id")));
                    case "name"      -> query.orderBy(asc ? cb.asc(root.get("name")) : cb.desc(root.get("name")));
                    case "createdAt" -> query.orderBy(asc ? cb.asc(root.get("createdAt")) : cb.desc(root.get("createdAt")));
                    default          -> query.orderBy(cb.asc(root.get("name")));
                }
            } else {
                query.orderBy(cb.asc(root.get("name")));
            }

            return cb.and(preds.toArray(new Predicate[0]));
        };
    }
}
