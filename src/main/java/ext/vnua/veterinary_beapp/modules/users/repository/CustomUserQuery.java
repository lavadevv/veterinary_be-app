package ext.vnua.veterinary_beapp.modules.users.repository;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.users.model.Role;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomUserQuery {
    private CustomUserQuery(){}

    @Data
    @NoArgsConstructor
    public static class UserFilterParam {
        private String keywords;
        private Boolean block;
        private String roleId;
        private String sortField;
        private String sortType;
    }

    public static Specification<User> getFilterUser(UserFilterParam param) {
        return (root, query, cb) -> {
            // === FETCH JOIN để tránh LazyInitializationException (chỉ áp dụng khi KHÔNG phải count) ===
            // Hibernate 6/Spring Data: query.getResultType() == Long.class cho count
            if (query.getResultType() != Long.class) {
                // fetch các quan hệ cần cho mapping DTO (name/id)
                root.fetch("department", JoinType.LEFT);
                root.fetch("position", JoinType.LEFT);
                root.fetch("role", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            if (param.keywords != null) {
                predicates.add(CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        root, cb, param.keywords, "fullName"));
            }
            if (param.block != null) {
                predicates.add(cb.equal(root.get("block"), param.block));
            }
            if (param.roleId != null) {
                Join<User, Role> userJoin = root.join("role");
                predicates.add(cb.equal(userJoin.get("id"), (param.roleId)));
            }

            // Sort
            if (param.sortField != null && !param.sortField.equals("")) {
                if (param.sortType == null || param.sortType.equals("") || Constant.SortType.DESC.equals(param.sortType)) {
                    query.orderBy(cb.desc(root.get(param.sortField)));
                } else if (Constant.SortType.ASC.equals(param.sortType)) {
                    query.orderBy(cb.asc(root.get(param.sortField)));
                }
            } else {
                query.orderBy(cb.desc(root.get("id")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
