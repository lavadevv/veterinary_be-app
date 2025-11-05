package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.WarehouseType;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomWarehouseTypeQuery {

    private CustomWarehouseTypeQuery(){}

    @Data
    @NoArgsConstructor
    public static class WareTypeFilterParam {
        private String keywords;   // search theo name
        private String sortField;  // "id" | "name"
        private String sortType;   // "ASC" | "DESC"
    }

    public static Specification<WarehouseType> getFilterWarehouseType(WareTypeFilterParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (param.getKeywords() != null && !param.getKeywords().trim().isEmpty()) {
                predicates.add(CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.getKeywords().trim(), "name"));
            }

            String sortField = param.getSortField();
            String sortType = param.getSortType();

            if (sortField != null && !sortField.isEmpty()) {
                if (Constant.SortType.ASC.equalsIgnoreCase(sortType)) {
                    query.orderBy(cb.asc(root.get(sortField)));
                } else {
                    query.orderBy(cb.desc(root.get(sortField)));
                }
            } else {
                query.orderBy(cb.asc(root.get("name"))); // mặc định A→Z
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
