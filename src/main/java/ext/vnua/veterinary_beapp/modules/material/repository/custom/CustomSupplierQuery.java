package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.Supplier;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import lombok.Data;
import lombok.NoArgsConstructor;

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
        /** ví dụ: "id", "supplierName", "manufacturerName", "manufacturerCode", ... */
        private String sortField;
        /** "ASC" | "DESC" */
        private String sortType;
    }

    public static Specification<Supplier> getFilterSupplier(SupplierFilterParam param) {
        return (root, query, cb) -> {
            // ========= FETCH để tránh LazyInitializationException (chỉ áp cho query không phải COUNT) =========
            Class<?> resultType = query.getResultType();
            boolean isCountQuery = Long.class.equals(resultType) || long.class.equals(resultType);
            if (!isCountQuery) {
                // fetch-join manufacturer giúp initialize quan hệ khi map DTO ở trong transaction hiện tại
                root.fetch("manufacturer", JoinType.LEFT);
                // do có join/fetch => cần distinct để loại trùng
                query.distinct(true);
            }

            // JOIN (không fetch) để dùng trong search/sort (COUNT query vẫn cần JOIN để điều kiện chạy được)
            Join<Object, Object> mfJoin = root.join("manufacturer", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            // ====== Keyword search ======
            if (param.getKeywords() != null && !param.getKeywords().trim().isEmpty()) {
                String kw = param.getKeywords().trim();
                String kwLike = "%" + kw.toLowerCase() + "%";

                // Supplier: supplierName, supplierCode (util insensitive có thể dùng unaccent nếu bạn đã cấu hình)
                Predicate pSupplierName = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, kw, "supplierName");
                Predicate pSupplierCode = CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, kw, "supplierCode");

                // Manufacturer: manufacturerName, manufacturerCode
                Predicate pMfName = cb.like(cb.lower(mfJoin.get("manufacturerName")), kwLike);
                Predicate pMfCode = cb.like(cb.lower(mfJoin.get("manufacturerCode")), kwLike);

                predicates.add(cb.or(pSupplierName, pSupplierCode, pMfName, pMfCode));
            }

            // ====== Các filter khác ======
            if (param.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), param.getIsActive()));
            }

            if (param.getCountryOfOrigin() != null && !param.getCountryOfOrigin().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("countryOfOrigin"), param.getCountryOfOrigin().trim()));
            }

            if (param.getGmpExpiryFromDate() != null && param.getGmpExpiryToDate() != null) {
                predicates.add(cb.between(root.get("gmpExpiryDate"),
                        param.getGmpExpiryFromDate(), param.getGmpExpiryToDate()));
            } else if (param.getGmpExpiryFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("gmpExpiryDate"), param.getGmpExpiryFromDate()));
            } else if (param.getGmpExpiryToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("gmpExpiryDate"), param.getGmpExpiryToDate()));
            }

            // ====== Sorting ======
            String sortField = param.getSortField();
            String sortType  = param.getSortType();
            if (sortField != null && !sortField.isBlank()) {
                boolean asc = Constant.SortType.ASC.equalsIgnoreCase(sortType);

                // sort theo field của Manufacturer
                if ("manufacturerName".equals(sortField)) {
                    query.orderBy(asc ? cb.asc(mfJoin.get("manufacturerName")) : cb.desc(mfJoin.get("manufacturerName")));
                } else if ("manufacturerCode".equals(sortField)) {
                    query.orderBy(asc ? cb.asc(mfJoin.get("manufacturerCode")) : cb.desc(mfJoin.get("manufacturerCode")));
                } else {
                    // sort theo field của Supplier (root)
                    query.orderBy(asc ? cb.asc(root.get(sortField)) : cb.desc(root.get(sortField)));
                }
            } else {
                // mặc định: id DESC
                query.orderBy(cb.desc(root.get("id")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
