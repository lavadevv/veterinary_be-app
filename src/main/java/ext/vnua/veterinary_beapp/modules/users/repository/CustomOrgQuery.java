package ext.vnua.veterinary_beapp.modules.users.repository;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.users.model.Department;
import ext.vnua.veterinary_beapp.modules.users.model.Position;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomOrgQuery {
    private CustomOrgQuery() {}

    @Data
    @NoArgsConstructor
    public static class OrgFilterParam {
        /** Tìm theo tên (LIKE, insensitive) */
        private String keywords;

        /** field để sort: "id" hoặc "name" */
        private String sortField;

        /** "ASC" | "DESC" | ""(mặc định DESC nếu null/empty) */
        private String sortType;
    }

    /* ===================== Department spec ===================== */

    public static Specification<Department> getFilterDepartment(OrgFilterParam param) {
        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (param.getKeywords() != null && !param.getKeywords().trim().isEmpty()) {
                predicates.add(CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.getKeywords().trim(), "name"));
            }

            // Sort
            String sortField = param.getSortField();
            String sortType = param.getSortType();
            if (sortField != null && !sortField.isEmpty()) {
                if (Constant.SortType.ASC.equalsIgnoreCase(sortType)) {
                    query.orderBy(cb.asc(root.get(sortField)));
                } else {
                    // default DESC
                    query.orderBy(cb.desc(root.get(sortField)));
                }
            } else {
                query.orderBy(cb.desc(root.get("id")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    /* ====================== Position spec ====================== */

    public static Specification<Position> getFilterPosition(OrgFilterParam param) {
        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (param.getKeywords() != null && !param.getKeywords().trim().isEmpty()) {
                predicates.add(CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.getKeywords().trim(), "name"));
            }

            // Sort
            String sortField = param.getSortField();
            String sortType = param.getSortType();
            if (sortField != null && !sortField.isEmpty()) {
                if (Constant.SortType.ASC.equalsIgnoreCase(sortType)) {
                    query.orderBy(cb.asc(root.get(sortField)));
                } else {
                    // default DESC
                    query.orderBy(cb.desc(root.get(sortField)));
                }
            } else {
                query.orderBy(cb.desc(root.get("id")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }
}
