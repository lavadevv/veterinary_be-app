package ext.vnua.veterinary_beapp.modules.product.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.product.enums.RegistrationStatus;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.model.ProductRegistration;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomProductRegistrationQuery {
    private CustomProductRegistrationQuery(){}

    @Data @NoArgsConstructor
    public static class ProductRegistrationFilterParam {
        private Long productId;
        private String registrationNumber;
        private RegistrationStatus status;
        private LocalDate expiringBefore;
        private String keywords; // productCode/productName/registrant/manufacturer
        private String sortField;
        private String sortType;
    }

    public static Specification<ProductRegistration> getFilter(ProductRegistrationFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (p.productId != null) {
                Join<ProductRegistration, Product> pj = root.join("product");
                ps.add(cb.equal(pj.get("id"), p.productId));
            }
            if (p.registrationNumber != null && !p.registrationNumber.isBlank()) {
                ps.add(cb.equal(root.get("registrationNumber"), p.registrationNumber));
            }
            if (p.status != null) {
                ps.add(cb.equal(root.get("status"), p.status));
            }
            if (p.expiringBefore != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("expiryDate"), p.expiringBefore));
            }
            if (p.keywords != null && !p.keywords.isBlank()) {
                Join<ProductRegistration, Product> pj = root.join("product");
                var prodCode = CriteriaBuilderUtil.createPredicateForSearchInsensitive(pj, cb, p.keywords, "productCode");
                var prodName = CriteriaBuilderUtil.createPredicateForSearchInsensitive(pj, cb, p.keywords, "productName");
                var regCom  = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.keywords, "registrantCompany");
                var manCom  = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.keywords, "manufacturerCompany");
                ps.add(cb.or(prodCode, prodName, regCom, manCom));
            }
            if (p.sortField != null && !p.sortField.isBlank()) {
                if (Constant.SortType.ASC.equals(p.sortType)) query.orderBy(cb.asc(root.get(p.sortField)));
                else query.orderBy(cb.desc(root.get(p.sortField)));
            } else query.orderBy(cb.desc(root.get("id")));

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}