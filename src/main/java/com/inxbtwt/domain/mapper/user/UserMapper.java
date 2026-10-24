package com.inxbtwt.domain.mapper.user;

import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.db.entity.AddressEntity;
import com.inxbtwt.domain.dto.user.http.AddressRequest;
import com.inxbtwt.domain.dto.user.http.AddressResponse;
import org.mapstruct.AfterMapping;
import com.inxbtwt.domain.dto.user.http.CreateUserRequest;
import com.inxbtwt.domain.dto.user.http.UserResponse;
import com.inxbtwt.domain.dto.user.http.UpdateUserRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    default UserDataEntity updateEntity(UserDataEntity entity, UpdateUserRequest request) {
        var builder = entity.toBuilder();
        updateBuilder(request, builder);
        return builder.build();
    }

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "password", source = "password")
    void updateBuilder(UpdateUserRequest request,
                       @MappingTarget UserDataEntity.UserDataEntityBuilder builder);

    UserResponse toResponse(UserDataEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(target = "coverUrl", ignore = true)
    @Mapping(target = "addresses", defaultExpression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserDataEntity toEntity(CreateUserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDefault", source = "isDefault")
    AddressEntity toAddressEntity(AddressRequest request);

    @Mapping(target = "isDefault", source = "default")
    AddressResponse toAddressResponse(AddressEntity entity);

    @AfterMapping
    default void linkAddresses(@MappingTarget UserDataEntity user) {
        user.getAddresses().forEach(address -> address.setUser(user));
    }
}
