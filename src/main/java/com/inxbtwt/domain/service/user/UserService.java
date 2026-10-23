package com.inxbtwt.domain.service.user;

import com.inxbtwt.domain.dto.user.http.CreateUserRequest;
import com.inxbtwt.domain.dto.user.http.UserResponse;
import com.inxbtwt.infra.exception.domain.user.UserException;
import com.inxbtwt.domain.dto.user.http.UpdateUserRequest;
import com.inxbtwt.domain.dto.user.http.PatchUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(CreateUserRequest createUserRequest) throws UserException;
    UserResponse getUser(UUID userId);
    Page<UserResponse> getUsers(Pageable pageable);
    UserResponse updateUser(UUID userId, UpdateUserRequest request);
    UserResponse patchUser(UUID userId, PatchUserRequest request);
}
