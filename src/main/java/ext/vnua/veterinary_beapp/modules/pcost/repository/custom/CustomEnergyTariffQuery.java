// ext/vnua/veterinary_beapp/modules/pcost/repository/custom/CustomEnergyTariffQuery.java
package ext.vnua.veterinary_beapp.modules.pcost.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.pcost.model.EnergyTariff;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomEnergyTariffQuery {
    private CustomEnergyTariffQuery(){}

    @Data @NoArgsConstructor
    public static class EnergyTariffFilterParam {
        /** Tìm theo code/name (không phân biệt hoa thường) */
        private String keywords;

        /** Chỉ lấy bản ghi đang active (is_active = true) */
        private Boolean isActive;

        /** Lọc theo khoảng ngày hiệu lực (effectiveDate) */
        private LocalDate fromDate;
        private LocalDate toDate;

        /** Sắp xếp */
        private String sortField; // vd: "effectiveDate", "id", "pricePerUnit"
        private String sortType;  // ASC | DESC

        /** Optional: lọc theo đúng code */
        private String code;
    }

    public static Specification<EnergyTariff> getFilter(EnergyTariffFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (p.getCode()!=null && !p.getCode().isBlank()) {
                ps.add(cb.equal(root.get("code"), p.getCode()));
            }
            if (p.getIsActive() != null) {
                ps.add(cb.equal(root.get("isActive"), p.getIsActive()));
            }
            if (p.getFromDate()!=null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("effectiveDate"), p.getFromDate()));
            }
            if (p.getToDate()!=null) {
                ps.add(cb.lessThanOrEqualTo(root.get("effectiveDate"), p.getToDate()));
            }
            if (p.getKeywords()!=null && !p.getKeywords().isBlank()) {
                var byCode = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.getKeywords(), "code");
                var byName = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, p.getKeywords(), "name");
                ps.add(cb.or(byCode, byName));
            }

            // sort mặc định
            if (p.getSortField()!=null && !p.getSortField().isBlank()) {
                if (Constant.SortType.ASC.equalsIgnoreCase(p.getSortType())) {
                    query.orderBy(cb.asc(root.get(p.getSortField())));
                } else {
                    query.orderBy(cb.desc(root.get(p.getSortField())));
                }
            } else {
                query.orderBy(cb.desc(root.get("id")));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
