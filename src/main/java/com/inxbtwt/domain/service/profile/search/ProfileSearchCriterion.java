package com.inxbtwt.domain.service.profile.search;

import com.inxbtwt.db.entity.UserDataEntity;
import org.springframework.data.jpa.domain.Specification;

public interface ProfileSearchCriterion {

    Specification<UserDataEntity> toSpecification(String query);
}
