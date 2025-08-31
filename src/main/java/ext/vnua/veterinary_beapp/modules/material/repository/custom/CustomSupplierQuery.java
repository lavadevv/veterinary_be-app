package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.Supplier;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomSupplierQuery {

    private CustomSupplierQuery() {}

    @Data
    @NoArgsConstructor
    public static class SupplierFilterParam {
        private String keywords;
        private Boolean isActive;
        private String countryOfOrigin;
        private LocalDate gmpExpiryFromDate;
        private LocalDate gmpExpiryToDate;
        private String sortField;
        private String sortType;
    }

    public static Specification<Supplier> getFilterSupplier(SupplierFilterParam param) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (param.keywords != null && !param.keywords.trim().isEmpty()) {
                Predicate supplierNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "supplierName");
                Predicate supplierCodePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "supplierCode");
                Predicate manufacturerNamePredicate = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, criteriaBuilder, param.keywords, "manufacturerName");

                predicates.add(criteriaBuilder.or(supplierNamePredicate, supplierCodePredicate, manufacturerNamePredicate));
            }

            if (param.isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), param.isActive));
            }

            if (param.countryOfOrigin != null && !param.countryOfOrigin.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("countryOfOrigin"), param.countryOfOrigin));
            }

            // Filter by GMP expiry date range
            if (param.gmpExpiryFromDate != null && param.gmpExpiryToDate != null) {
                predicates.add(criteriaBuilder.between(root.get("gmpExpiryDate"),
                        param.gmpExpiryFromDate, param.gmpExpiryToDate));
            } else if (param.gmpExpiryFromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("gmpExpiryDate"), param.gmpExpiryFromDate));
            } else if (param.gmpExpiryToDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("gmpExpiryDate"), param.gmpExpiryToDate));
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
