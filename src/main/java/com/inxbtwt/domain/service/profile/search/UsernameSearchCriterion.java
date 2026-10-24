package com.inxbtwt.domain.service.profile.search;

import com.inxbtwt.db.entity.UserDataEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class UsernameSearchCriterion implements ProfileSearchCriterion {

    @Override
    public Specification<UserDataEntity> toSpecification(String query) {
        String escaped = query.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        String pattern = "%" + escaped + "%";

        return (root, criteriaQuery, builder) -> builder.like(
                builder.lower(root.get("username")), pattern, '\\');
    }
}
