package com.inxbtwt.domain.service.user;

import com.inxbtwt.domain.dto.user.http.CreateUserRequest;
import com.inxbtwt.domain.dto.user.http.UserResponse;
import com.inxbtwt.infra.exception.domain.user.UserException;
import com.inxbtwt.domain.dto.user.http.UpdateUserRequest;
import com.inxbtwt.domain.dto.user.http.PatchUserRequest;
import com.inxbtwt.domain.dto.user.http.FieldAvailabilityResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;
import com.inxbtwt.domain.dto.bucket.UploadFileResponse;
import org.springframework.web.multipart.MultipartFile;
import com.inxbtwt.domain.dto.bucket.DownloadFileResponse;
import com.inxbtwt.domain.dto.user.http.UserPhotoUrlsResponse;
import com.inxbtwt.domain.enums.UserPhotoType;

public interface UserService {
    FieldAvailabilityResponse getAvailability(String field, String value);

    UserResponse createUser(CreateUserRequest createUserRequest) throws UserException;
    UploadFileResponse uploadPhoto(UUID userId, MultipartFile photo, UserPhotoType type);
    UserPhotoUrlsResponse getPhotoUrls(UUID userId);
    DownloadFileResponse downloadPhoto(UUID userId, String url);
    UserResponse getUser(UUID userId);
    Page<UserResponse> getUsers(Pageable pageable);
    UserResponse updateUser(UUID userId, UpdateUserRequest request);
    UserResponse patchUser(UUID userId, PatchUserRequest request);
}
