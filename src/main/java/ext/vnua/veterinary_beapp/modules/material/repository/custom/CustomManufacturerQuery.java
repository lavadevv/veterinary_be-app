package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.Manufacturer;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomManufacturerQuery {

    private CustomManufacturerQuery(){}

    @Data
    @NoArgsConstructor
    public static class ManuFilterParam {
        private String keywords;     // search theo code/name/distributor/country
        private Boolean isActive;    // filter trạng thái
        private String countryOfOrigin;

        private String sortField;    // id | manufacturerName | manufacturerCode | countryOfOrigin | officialDistributorName
        private String sortType;     // ASC | DESC
    }

    public static Specification<Manufacturer> getFilterManufacturer(ManuFilterParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (param.getKeywords() != null && !param.getKeywords().trim().isEmpty()) {
                String kw = param.getKeywords().trim();
                Predicate byName = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, kw, "manufacturerName");
                Predicate byCode = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, kw, "manufacturerCode");
                Predicate byDistributor = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, kw, "officialDistributorName");
                Predicate byCountry = CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, cb, kw, "countryOfOrigin");
                predicates.add(cb.or(byName, byCode, byDistributor, byCountry));
            }

            if (param.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), param.getIsActive()));
            }

            if (param.getCountryOfOrigin() != null && !param.getCountryOfOrigin().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("countryOfOrigin"), param.getCountryOfOrigin().trim()));
            }

            if (param.getSortField() != null && !param.getSortField().isBlank()) {
                boolean asc = Constant.SortType.ASC.equalsIgnoreCase(param.getSortType());
                if (asc) {
                    query.orderBy(cb.asc(root.get(param.getSortField())));
                } else {
                    query.orderBy(cb.desc(root.get(param.getSortField())));
                }
            } else {
                query.orderBy(cb.desc(root.get("id"))); // mặc định id DESC
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
