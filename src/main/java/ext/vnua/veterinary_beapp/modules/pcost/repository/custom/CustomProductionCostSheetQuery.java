package ext.vnua.veterinary_beapp.modules.pcost.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.pcost.model.ProductionCostSheet;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomProductionCostSheetQuery {
    private CustomProductionCostSheetQuery(){}

    @Data @NoArgsConstructor
    public static class ProductionCostSheetFilterParam {
        private Long productId;
        private String sheetCode;
        private String sheetName;
        private Boolean isActive;
        private LocalDate fromEffective;
        private LocalDate toEffective;
        private String keywords;   // search sheetCode/sheetName
        private String sortField;  // ví dụ: "effectiveDate","sheetCode","id"
        private String sortType;   // "ASC" | "DESC"
    }

    public static Specification<ProductionCostSheet> getFilter(ProductionCostSheetFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (p.getProductId() != null) {
                ps.add(cb.equal(root.get("productId"), p.getProductId()));
            }
            if (p.getSheetCode()!=null && !p.getSheetCode().isBlank()) {
                ps.add(cb.equal(root.get("sheetCode"), p.getSheetCode().trim()));
            }
            if (p.getSheetName()!=null && !p.getSheetName().isBlank()) {
                // match exactly, nếu muốn like thì đổi sang like
                ps.add(cb.equal(root.get("sheetName"), p.getSheetName().trim()));
            }
            if (p.getIsActive()!=null) {
                ps.add(cb.equal(root.get("isActive"), p.getIsActive()));
            }
            if (p.getFromEffective()!=null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("effectiveDate"), p.getFromEffective()));
            }
            if (p.getToEffective()!=null) {
                ps.add(cb.lessThanOrEqualTo(root.get("effectiveDate"), p.getToEffective()));
            }
            if (p.getKeywords()!=null && !p.getKeywords().isBlank()) {
                Predicate codeLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.getKeywords(), "sheetCode");
                Predicate nameLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.getKeywords(), "sheetName");
                ps.add(cb.or(codeLike, nameLike));
            }

            // Sort
            if (p.getSortField()!=null && !p.getSortField().isBlank()) {
                if (Constant.SortType.ASC.equals(p.getSortType())) {
                    query.orderBy(cb.asc(root.get(p.getSortField())));
                } else {
                    query.orderBy(cb.desc(root.get(p.getSortField())));
                }
            } else {
                // Mặc định: effectiveDate DESC, rồi id DESC
                query.orderBy(cb.desc(root.get("effectiveDate")), cb.desc(root.get("id")));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
