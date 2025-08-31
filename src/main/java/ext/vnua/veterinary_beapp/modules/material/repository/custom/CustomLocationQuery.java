package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.model.Warehouse;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomLocationQuery {

    private CustomLocationQuery() {}

    @Data
    @NoArgsConstructor
    public static class LocationFilterParam {
        private String keywords;
        private Long warehouseId;
        private Boolean isAvailable;
        private Double minCapacity;
        private Double maxCapacity;
        private String sortField;
        private String sortType;
    }

    public static Specification<Location> getFilterLocation(LocationFilterParam param) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (param.keywords != null && !param.keywords.trim().isEmpty()) {
                Predicate locationCodePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "locationCode");
                Predicate shelfPredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "shelf");

                predicates.add(criteriaBuilder.or(locationCodePredicate, shelfPredicate));
            }

            if (param.warehouseId != null) {
                Join<Location, Warehouse> warehouseJoin = root.join("warehouse");
                predicates.add(criteriaBuilder.equal(warehouseJoin.get("id"), param.warehouseId));
            }

            if (param.isAvailable != null) {
                predicates.add(criteriaBuilder.equal(root.get("isAvailable"), param.isAvailable));
            }

            // Filter by capacity range
            if (param.minCapacity != null && param.maxCapacity != null) {
                predicates.add(criteriaBuilder.between(root.get("maxCapacity"), param.minCapacity, param.maxCapacity));
            } else if (param.minCapacity != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("maxCapacity"), param.minCapacity));
            } else if (param.maxCapacity != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("maxCapacity"), param.maxCapacity));
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

