package ext.vnua.veterinary_beapp.modules.productionplan.repository.custom;

import ext.vnua.veterinary_beapp.modules.productionplan.enums.ProductionPlanStatus;
import ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionLot;
import ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionPlan;
import ext.vnua.veterinary_beapp.modules.product.model.FormulaHeader;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomProductionLotQuery {
    private CustomProductionLotQuery() {}

    @Data
    @NoArgsConstructor
    public static class ProductionLotFilterParam {
        private String lotNumber;
        private ProductionPlanStatus status;
        private LocalDate fromDate;
        private LocalDate toDate;
        private String keywords; // matches lotNumber or formulaCode/name
    }

    public static Specification<ProductionLot> getFilter(ProductionLotFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (p == null) return cb.and();

            if (StringUtils.hasText(p.getLotNumber())) {
                String kw = "%" + p.getLotNumber().trim().toLowerCase() + "%";
                ps.add(cb.like(cb.lower(root.get("lotNumber")), kw));
            }

            if (p.getStatus() != null) {
                ps.add(cb.equal(root.get("status"), p.getStatus()));
            }

            if (p.getFromDate() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("planDate"), p.getFromDate()));
            }
            if (p.getToDate() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("planDate"), p.getToDate()));
            }

            if (StringUtils.hasText(p.getKeywords())) {
                String kw = "%" + p.getKeywords().trim().toLowerCase() + "%";
                query.distinct(true);
                Join<ProductionLot, ProductionPlan> plans = root.join("plans", JoinType.LEFT);
                Join<ProductionPlan, ProductFormula> formulaJoin = plans.join("formula", JoinType.LEFT);
                Join<ProductFormula, FormulaHeader> headerJoin = formulaJoin.join("header", JoinType.LEFT);
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("lotNumber")), kw),
                        cb.like(cb.lower(headerJoin.get("formulaCode")), kw),
                        cb.like(cb.lower(headerJoin.get("formulaName")), kw)
                ));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}

