package com.inxbtwt.infra.controller.profile;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.profile.http.PatchProfileRequest;
import com.inxbtwt.domain.dto.profile.http.ProfileResponse;
import com.inxbtwt.domain.dto.profile.http.ProfileSummaryResponse;
import com.inxbtwt.domain.dto.profile.http.ProfileViewResponse;
import com.inxbtwt.domain.service.profile.ProfileService;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import com.inxbtwt.infra.exception.http.HttpMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProfileSummaryResponse>>> searchProfiles(
            @RequestParam @Size(max = 255, message = "A pesquisa deve ter no máximo 255 caracteres.") String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var pageable = PageRequest.of(page, size, Sort.by("username").ascending().and(Sort.by("userId")));
        return ApiControllerUtils.ok(HttpMessages.PROFILES_FOUND, profileService.searchProfiles(query, pageable));
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
