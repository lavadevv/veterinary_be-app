package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.Warehouse;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomWarehouseQuery {

    private CustomWarehouseQuery() {}

    @Data
    @NoArgsConstructor
    public static class WarehouseFilterParam {
        private String keywords;
        private String warehouseType;
        private Boolean isActive;
        private String sortField;
        private String sortType;
    }

    public static Specification<Warehouse> getFilterWarehouse(WarehouseFilterParam param) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (param.keywords != null && !param.keywords.trim().isEmpty()) {
                Predicate warehouseNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "warehouseName");
                Predicate warehouseCodePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "warehouseCode");

                predicates.add(criteriaBuilder.or(warehouseNamePredicate, warehouseCodePredicate));
            }

            if (param.warehouseType != null && !param.warehouseType.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("warehouseType"), param.warehouseType));
            }

            if (param.isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), param.isActive));
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
