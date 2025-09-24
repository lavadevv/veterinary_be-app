package ext.vnua.veterinary_beapp.modules.production.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.production.enums.ProductionOrderStatus;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionLine;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomProductionOrderQuery {
    private CustomProductionOrderQuery(){}

    @Data @NoArgsConstructor
    public static class ProductionOrderFilterParam {
        private String orderCode;
        private ProductionOrderStatus status;
        private Long productId;
        private Long lineId;
        private LocalDate fromPlannedStart;
        private LocalDate toPlannedStart;
        private LocalDate fromPlannedEnd;
        private LocalDate toPlannedEnd;
        private String keywords; // orderCode / productCode / productName / lineCode / lineName
        private String sortField;
        private String sortType;
    }

    public static Specification<ProductionOrder> getFilter(ProductionOrderFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (p.orderCode != null && !p.orderCode.isBlank()) {
                ps.add(cb.equal(root.get("orderCode"), p.orderCode));
            }
            if (p.status != null) {
                ps.add(cb.equal(root.get("status"), p.status));
            }
            if (p.productId != null) {
                Join<ProductionOrder, Product> pj = root.join("product");
                ps.add(cb.equal(pj.get("id"), p.productId));
            }
            if (p.lineId != null) {
                Join<ProductionOrder, ProductionLine> lj = root.join("productionLine");
                ps.add(cb.equal(lj.get("id"), p.lineId));
            }
            if (p.fromPlannedStart != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("plannedStartDate"), p.fromPlannedStart));
            }
            if (p.toPlannedStart != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("plannedStartDate"), p.toPlannedStart));
            }
            if (p.fromPlannedEnd != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("plannedEndDate"), p.fromPlannedEnd));
            }
            if (p.toPlannedEnd != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("plannedEndDate"), p.toPlannedEnd));
            }
            if (p.keywords != null && !p.keywords.isBlank()) {
                var codeLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.keywords, "orderCode");
                Join<ProductionOrder, Product> pj = root.join("product");
                var prodCodeLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(pj, cb, p.keywords, "productCode");
                var prodNameLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(pj, cb, p.keywords, "productName");
                Join<ProductionOrder, ProductionLine> lj = root.join("productionLine", jakarta.persistence.criteria.JoinType.LEFT);
                var lineCodeLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(lj, cb, p.keywords, "lineCode");
                var lineNameLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(lj, cb, p.keywords, "name");
                ps.add(cb.or(codeLike, prodCodeLike, prodNameLike, lineCodeLike, lineNameLike));
            }

            // sort
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
