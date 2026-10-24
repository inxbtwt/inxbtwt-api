package com.inxbtwt.infra.controller.profile;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.profile.http.PatchProfileRequest;
import com.inxbtwt.domain.dto.profile.http.ProfileResponse;
import com.inxbtwt.domain.service.profile.ProfileService;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import com.inxbtwt.infra.exception.http.HttpMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        return ApiControllerUtils.ok(HttpMessages.PROFILE_FOUND, profileService.getProfile());
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> patchProfile(
            @Valid @RequestBody PatchProfileRequest request) {
        return ApiControllerUtils.ok(HttpMessages.PROFILE_UPDATED, profileService.patchProfile(request));
    }
}
