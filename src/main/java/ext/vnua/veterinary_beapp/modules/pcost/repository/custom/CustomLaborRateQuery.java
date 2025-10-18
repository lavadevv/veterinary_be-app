package ext.vnua.veterinary_beapp.modules.pcost.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.pcost.model.LaborRate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomLaborRateQuery {
    private CustomLaborRateQuery() {}

    @Data @NoArgsConstructor
    public static class LaborRateFilterParam {
        private String keywords;   // tìm code/name (insensitive)
        private String code;       // match exact
        private Boolean isActive;  // true/false
        private LocalDate fromDate; // effectiveDate >=
        private LocalDate toDate;   // effectiveDate <=
        private String sortField;
        private String sortType;   // ASC/DESC
    }

    public static Specification<LaborRate> getFilter(LaborRateFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (p.code != null && !p.code.isBlank()) {
                ps.add(cb.equal(root.get("code"), p.code.trim()));
            }
            if (p.isActive != null) {
                ps.add(cb.equal(root.get("isActive"), p.isActive));
            }
            if (p.fromDate != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("effectiveDate"), p.fromDate));
            }
            if (p.toDate != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("effectiveDate"), p.toDate));
            }
            if (p.keywords != null && !p.keywords.isBlank()) {
                var codeLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.keywords, "code");
                var nameLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.keywords, "name");
                ps.add(cb.or(codeLike, nameLike));
            }

            if (p.sortField != null && !p.sortField.isBlank()) {
                if (Constant.SortType.ASC.equalsIgnoreCase(p.sortType)) query.orderBy(cb.asc(root.get(p.sortField)));
                else query.orderBy(cb.desc(root.get(p.sortField)));
            } else {
                query.orderBy(cb.desc(root.get("effectiveDate")), cb.desc(root.get("id")));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
