// File: ext/vnua/veterinary_beapp/modules/product/repository/custom/CustomProductFormulaQuery.java
package ext.vnua.veterinary_beapp.modules.product.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.product.enums.FormulationType;
import ext.vnua.veterinary_beapp.modules.product.enums.ProductCategory;
import ext.vnua.veterinary_beapp.modules.product.model.FormulaHeader;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Specification filter cho danh sách phiên bản công thức (qua header + products) */
public class CustomProductFormulaQuery {
    private CustomProductFormulaQuery(){}

    @Data @NoArgsConstructor
    public static class ProductFormulaFilterParam {
        private Long productId;
        private String productCode;
        private String productName;
        private String version;
        private Boolean active;

        private LocalDate fromCreatedDate;
        private LocalDate toCreatedDate;

        private String keywords;
        private String productLabel;

        private ProductCategory productCategory;
        private FormulationType formulationType;

        private String sortField; // createdDate, version, isActive
        private String sortType;  // ASC | DESC
    }

    public static Specification<ProductFormula> getFilter(ProductFormulaFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            // join header & products
            Join<ProductFormula, FormulaHeader> hj = root.join("header");
            Join<FormulaHeader, Product> pj = hj.join("products", JoinType.LEFT);

            if (p.getProductId() != null) {
                ps.add(cb.equal(pj.get("id"), p.getProductId()));
            }
            if (p.getProductCode() != null && !p.getProductCode().isBlank()) {
                ps.add(CriteriaBuilderUtil.createPredicateForSearchInsensitive(pj, cb, p.getProductCode(), "productCode"));
            }
            if (p.getProductName() != null && !p.getProductName().isBlank()) {
                ps.add(CriteriaBuilderUtil.createPredicateForSearchInsensitive(pj, cb, p.getProductName(), "productName"));
            }
            if (p.getVersion() != null && !p.getVersion().isBlank()) {
                ps.add(CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.getVersion(), "version"));
            }
            if (p.getActive() != null) {
                ps.add(cb.equal(root.get("isActive"), p.getActive()));
            }

            if (p.getFromCreatedDate() != null) {
                LocalDateTime from = p.getFromCreatedDate().atStartOfDay();
                ps.add(cb.greaterThanOrEqualTo(root.get("createdDate"), from));
            }
            if (p.getToCreatedDate() != null) {
                LocalDateTime to = p.getToCreatedDate().plusDays(1).atStartOfDay().minusNanos(1);
                ps.add(cb.lessThanOrEqualTo(root.get("createdDate"), to));
            }

            if (p.getProductLabel() != null && !p.getProductLabel().isBlank()) {
                ps.add(CriteriaBuilderUtil.createPredicateForSearchInsensitive(pj, cb, p.getProductLabel(), "brandName"));
            }

            if (p.getKeywords() != null && !p.getKeywords().isBlank()) {
                Predicate codeLike   = CriteriaBuilderUtil.createPredicateForSearchInsensitive(pj, cb, p.getKeywords(), "productCode");
                Predicate nameLike   = CriteriaBuilderUtil.createPredicateForSearchInsensitive(pj, cb, p.getKeywords(), "productName");
                Predicate verLike    = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.getKeywords(), "version");
                // allow search by header formulaCode/formulaName as well
                Predicate hCodeLike  = CriteriaBuilderUtil.createPredicateForSearchInsensitive(hj, cb, p.getKeywords(), "formulaCode");
                Predicate hNameLike  = CriteriaBuilderUtil.createPredicateForSearchInsensitive(hj, cb, p.getKeywords(), "formulaName");
                ps.add(cb.or(codeLike, nameLike, verLike, hCodeLike, hNameLike));
            }

            if (p.getProductCategory() != null) {
                ps.add(cb.equal(pj.get("productCategory"), p.getProductCategory()));
            }
            if (p.getFormulationType() != null) {
                ps.add(cb.equal(pj.get("formulationType"), p.getFormulationType()));
            }

            // sort
            if (p.getSortField() != null && !p.getSortField().isBlank()) {
                if (Constant.SortType.ASC.equalsIgnoreCase(p.getSortType())) {
                    query.orderBy(cb.asc(root.get(p.getSortField())));
                } else {
                    query.orderBy(cb.desc(root.get(p.getSortField())));
                }
            } else {
                query.orderBy(cb.desc(root.get("createdDate")));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
