package com.inxbtwt.infra.controller.user;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.bucket.UploadFileResponse;
import com.inxbtwt.domain.dto.user.http.UserPhotoUrlsResponse;
import com.inxbtwt.domain.enums.UserPhotoType;
import com.inxbtwt.infra.exception.http.HttpMessages;
import com.inxbtwt.domain.dto.user.http.CreateUserRequest;
import com.inxbtwt.domain.dto.user.http.UserResponse;
import com.inxbtwt.domain.dto.user.http.UpdateUserRequest;
import com.inxbtwt.domain.dto.user.http.PatchUserRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.UUID;
import com.inxbtwt.domain.service.user.UserService;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping(value = "/{userId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadFileResponse>> uploadPhoto(
            @PathVariable UUID userId, @RequestPart("photo") MultipartFile photo,
            @RequestParam(defaultValue = "PROFILE") UserPhotoType type) {
        return ApiControllerUtils.ok(HttpMessages.USER_PHOTO_UPDATED, userService.uploadPhoto(userId, photo, type));
    }

    @GetMapping("/{userId}/photos")
    public ResponseEntity<ApiResponse<UserPhotoUrlsResponse>> getPhotoUrls(@PathVariable UUID userId) {
        return ApiControllerUtils.ok(HttpMessages.USER_PHOTOS_FOUND, userService.getPhotoUrls(userId));
    }

    @GetMapping("/{userId}/photo")
    public ResponseEntity<byte[]> downloadPhoto(@PathVariable UUID userId, @RequestParam String url) {
        var photo = userService.downloadPhoto(userId, url);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(photo.contentType()))
                .contentLength(photo.size()).header("X-Content-Type-Options", "nosniff")
                .cacheControl(org.springframework.http.CacheControl.noStore()).body(photo.data());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID userId) {
        return ApiControllerUtils.ok(HttpMessages.USER_FOUND, userService.getUser(userId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página não pode ser negativa.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "O tamanho da página deve ser pelo menos 1.")
            @Max(value = 100, message = "O tamanho da página deve ser no máximo 100.") int size) {
        return ApiControllerUtils.ok(HttpMessages.USERS_FOUND,
                userService.getUsers(PageRequest.of(page, size, Sort.by("id").ascending())));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest request) {
        return ApiControllerUtils.ok(HttpMessages.USER_UPDATED, userService.updateUser(userId, request));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> patchUser(
            @PathVariable UUID userId, @RequestBody PatchUserRequest request) {
        return ApiControllerUtils.ok(HttpMessages.USER_UPDATED, userService.patchUser(userId, request));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiControllerUtils.created(HttpMessages.USER_CREATED, userService.createUser(request));
    }
}
