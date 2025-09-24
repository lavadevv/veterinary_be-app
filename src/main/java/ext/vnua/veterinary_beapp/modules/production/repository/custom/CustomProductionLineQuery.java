package ext.vnua.veterinary_beapp.modules.production.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionLine;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomProductionLineQuery {
    private CustomProductionLineQuery(){}

    @Data @NoArgsConstructor
    public static class ProductionLineFilterParam {
        private String lineCode;
        private String name;
        private String status; // ACTIVE / INACTIVE / UNDER_MAINTENANCE
        private String keywords; // lineCode / name
        private String sortField;
        private String sortType;
    }

    public static Specification<ProductionLine> getFilter(ProductionLineFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (p.lineCode != null && !p.lineCode.isBlank()) {
                ps.add(cb.equal(root.get("lineCode"), p.lineCode));
            }
            if (p.name != null && !p.name.isBlank()) {
                ps.add(cb.like(cb.lower(root.get("name")), "%" + p.name.toLowerCase() + "%"));
            }
            if (p.status != null && !p.status.isBlank()) {
                ps.add(cb.equal(root.get("status"), p.status));
            }
            if (p.keywords != null && !p.keywords.isBlank()) {
                var codeLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.keywords, "lineCode");
                var nameLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.keywords, "name");
                ps.add(cb.or(codeLike, nameLike));
            }

            if (p.sortField != null && !p.sortField.isBlank()) {
                if (Constant.SortType.ASC.equals(p.sortType)) query.orderBy(cb.asc(root.get(p.sortField)));
                else query.orderBy(cb.desc(root.get(p.sortField)));
            } else {
                query.orderBy(cb.desc(root.get("id")));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
