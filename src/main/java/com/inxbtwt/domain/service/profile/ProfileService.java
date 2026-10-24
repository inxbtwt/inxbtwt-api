package com.inxbtwt.domain.service.profile;

import com.inxbtwt.domain.dto.profile.http.PatchProfileRequest;
import com.inxbtwt.domain.dto.profile.http.ProfileResponse;

public interface ProfileService {

    ProfileResponse getProfile();

    ProfileResponse patchProfile(PatchProfileRequest request);
}
