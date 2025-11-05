package ext.vnua.veterinary_beapp.modules.productionplan.repository.custom;

import ext.vnua.veterinary_beapp.modules.productionplan.enums.ProductionPlanStatus;
import ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionPlan;
import ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionPlanProduct;
import ext.vnua.veterinary_beapp.modules.product.model.FormulaHeader;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Specification filter for ProductionPlan listing/search (aligns with other modules' pattern) */
public class CustomProductionPlanQuery {
    private CustomProductionPlanQuery() {}

    @Data
    @NoArgsConstructor
    public static class ProductionPlanFilterParam {
        private String lotNumber;
        private String keywords;
        private Long formulaId;
        private Long productId;
        private ProductionPlanStatus status;
        private LocalDate fromDate;
        private LocalDate toDate;

        private String sortField; // e.g., planDate, createdDate, lotNumber
        private String sortType;  // ASC | DESC
    }

    public static Specification<ProductionPlan> getFilter(ProductionPlanFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (p == null) return cb.and();

            if (StringUtils.hasText(p.getLotNumber())) {
                String kw = "%" + p.getLotNumber().trim().toLowerCase() + "%";
                Join<ProductionPlan, ?> lotJoin = root.join("lot", JoinType.LEFT);
                ps.add(cb.like(cb.lower(lotJoin.get("lotNumber")), kw));
            }

            if (p.getFormulaId() != null) {
                ps.add(cb.equal(root.get("formula").get("id"), p.getFormulaId()));
            }

            if (p.getStatus() != null) {
                ps.add(cb.equal(root.get("status"), p.getStatus()));
            }

            if (p.getFromDate() != null) {
                Join<ProductionPlan, ?> lotJoin = root.join("lot", JoinType.LEFT);
                ps.add(cb.greaterThanOrEqualTo(lotJoin.get("planDate"), p.getFromDate()));
            }
            if (p.getToDate() != null) {
                Join<ProductionPlan, ?> lotJoin = root.join("lot", JoinType.LEFT);
                ps.add(cb.lessThanOrEqualTo(lotJoin.get("planDate"), p.getToDate()));
            }

            if (p.getProductId() != null) {
                query.distinct(true);
                Join<ProductionPlan, ProductionPlanProduct> productLines = root.join("productLines", JoinType.INNER);
                ps.add(cb.equal(productLines.get("product").get("id"), p.getProductId()));
            }

            if (StringUtils.hasText(p.getKeywords())) {
                String kw = "%" + p.getKeywords().trim().toLowerCase() + "%";
                query.distinct(true);
                Join<ProductionPlan, ProductionPlanProduct> productLines = root.join("productLines", JoinType.LEFT);
                Join<ProductionPlanProduct, Product> productJoin = productLines.join("product", JoinType.LEFT);
                Join<ProductionPlan, ProductFormula> formulaJoin = root.join("formula", JoinType.LEFT);
                Join<ProductFormula, FormulaHeader> headerJoin = formulaJoin.join("header", JoinType.LEFT);

                Join<ProductionPlan, ?> lotJoinKw = root.join("lot", JoinType.LEFT);
                ps.add(cb.or(
                        cb.like(cb.lower(lotJoinKw.get("lotNumber")), kw),
                        cb.like(cb.lower(headerJoin.get("formulaCode")), kw),
                        cb.like(cb.lower(headerJoin.get("formulaName")), kw),
                        cb.like(cb.lower(productJoin.get("productCode")), kw),
                        cb.like(cb.lower(productJoin.get("productName")), kw)
                ));
            }

            // Note: sorting is typically applied by the service/repository caller via Pageable.
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
