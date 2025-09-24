package ext.vnua.veterinary_beapp.modules.product.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.product.enums.ProductBatchStatus;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBatch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomProductBatchQuery {
    private CustomProductBatchQuery(){}

    @Data @NoArgsConstructor
    public static class ProductBatchFilterParam {
        private Long productId;
        private String batchNumber;
        private ProductBatchStatus status;
        private LocalDate fromManufacturing;
        private LocalDate toManufacturing;
        private LocalDate toExpiry;
        private String keywords; // search productName/productCode/batchNumber
        private String sortField;
        private String sortType;
    }

    public static Specification<ProductBatch> getFilter(ProductBatchFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (p.productId != null) {
                Join<ProductBatch, Product> pj = root.join("product");
                ps.add(cb.equal(pj.get("id"), p.productId));
            }
            if (p.batchNumber != null && !p.batchNumber.isBlank()) {
                ps.add(cb.equal(root.get("batchNumber"), p.batchNumber));
            }
            if (p.status != null) {
                ps.add(cb.equal(root.get("status"), p.status));
            }
            if (p.fromManufacturing != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("manufacturingDate"), p.fromManufacturing));
            }
            if (p.toManufacturing != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("manufacturingDate"), p.toManufacturing));
            }
            if (p.toExpiry != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("expiryDate"), p.toExpiry));
            }
            if (p.keywords != null && !p.keywords.isBlank()) {
                Predicate batchCodeLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.keywords, "batchNumber");
                Join<ProductBatch, Product> pj = root.join("product");
                Predicate prodCodeLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(pj, cb, p.keywords, "productCode");
                Predicate prodNameLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(pj, cb, p.keywords, "productName");
                ps.add(cb.or(batchCodeLike, prodCodeLike, prodNameLike));
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