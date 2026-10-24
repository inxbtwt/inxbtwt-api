package com.inxbtwt.domain.service.profile;

import com.inxbtwt.domain.dto.profile.http.PatchProfileRequest;
import com.inxbtwt.domain.dto.profile.http.ProfileResponse;
import com.inxbtwt.domain.dto.profile.http.ProfileSummaryResponse;
import com.inxbtwt.domain.dto.profile.http.ProfileViewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProfileService {

    ProfileResponse getProfile();

    Page<ProfileSummaryResponse> searchProfiles(String query, Pageable pageable);

    ProfileViewResponse getProfileByUsername(String username);

    ProfileResponse patchProfile(PatchProfileRequest request);
}
