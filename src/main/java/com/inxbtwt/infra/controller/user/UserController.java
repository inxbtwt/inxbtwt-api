package com.inxbtwt.infra.controller.user;

import com.inxbtwt.domain.dto.api.ApiResponse;
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
import jakarta.validation.Valid;
import io.opentelemetry.api.trace.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID userId) {
        return ok(HttpMessages.USER_FOUND, userService.getUser(userId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página não pode ser negativa.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "O tamanho da página deve ser pelo menos 1.")
            @Max(value = 100, message = "O tamanho da página deve ser no máximo 100.") int size) {
        return ok(HttpMessages.USERS_FOUND,
                userService.getUsers(PageRequest.of(page, size, Sort.by("id").ascending())));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest request) {
        return ok(HttpMessages.USER_UPDATED, userService.updateUser(userId, request));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> patchUser(
            @PathVariable UUID userId, @RequestBody PatchUserRequest request) {
        return ok(HttpMessages.USER_UPDATED, userService.patchUser(userId, request));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.<T>builder()
                .message(message).data(data).status(StatusCode.OK).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        var response = ApiResponse.<UserResponse>builder()
                .message(HttpMessages.USER_CREATED)
                .data(userService.createUser(request))
                .status(StatusCode.OK)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
