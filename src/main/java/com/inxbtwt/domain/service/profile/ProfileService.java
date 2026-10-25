package com.inxbtwt.domain.service.profile;

import com.inxbtwt.domain.dto.profile.http.PatchProfileRequest;
import com.inxbtwt.domain.dto.profile.http.ProfileResponse;
import com.inxbtwt.domain.dto.profile.http.ProfileViewResponse;

public interface ProfileService {

    ProfileResponse getProfile();

    ProfileViewResponse getProfileByUsername(String username);

    ProfileResponse patchProfile(PatchProfileRequest request);
}
