package com.inxbtwt.domain.service.user.find;

import com.inxbtwt.db.entity.UserDataEntity;

import java.util.UUID;

public interface FindUserUseCase {
    UserDataEntity findUser(UUID userId);
}
