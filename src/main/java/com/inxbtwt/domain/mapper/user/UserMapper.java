package com.inxbtwt.domain.mapper.user;

import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.domain.dto.user.http.CreateUserRequest;
import com.inxbtwt.domain.dto.user.http.UserResponse;
import com.inxbtwt.domain.dto.user.http.UpdateUserRequest;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDataEntity updateEntity(UserDataEntity entity, UpdateUserRequest request) {
        return entity.toBuilder()
                .name(request.name())
                .photoUrl(request.photoUrl())
                .email(request.email())
                .phone(request.phone())
                .password(request.password())
                .build();
    }

    public UserResponse toResponse(UserDataEntity entity) {
        return UserResponse.builder()
                .userId(entity.getUserId())
                .name(entity.getName())
                .photoUrl(entity.getPhotoUrl())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserDataEntity toEntity(CreateUserRequest request) {
        return UserDataEntity.builder()
                .name(request.name())
                .photoUrl(request.photoUrl())
                .email(request.email())
                .phone(request.phone())
                .password(request.password())
                .build();
    }
}
