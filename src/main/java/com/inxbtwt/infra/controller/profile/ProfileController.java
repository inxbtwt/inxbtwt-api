package com.inxbtwt.infra.controller.profile;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.profile.http.PatchProfileRequest;
import com.inxbtwt.domain.dto.profile.http.ProfileResponse;
import com.inxbtwt.domain.dto.profile.http.ProfileViewResponse;
import com.inxbtwt.domain.service.profile.ProfileService;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import com.inxbtwt.infra.exception.http.HttpMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        return ApiControllerUtils.ok(HttpMessages.PROFILE_FOUND, profileService.getProfile());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<ProfileViewResponse>> getProfileByUsername(@PathVariable String username) {
        return ApiControllerUtils.ok(HttpMessages.PROFILE_FOUND, profileService.getProfileByUsername(username));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> patchProfile(
            @Valid @RequestBody PatchProfileRequest request) {
        return ApiControllerUtils.ok(HttpMessages.PROFILE_UPDATED, profileService.patchProfile(request));
    }
}
