package com.inxbtwt.domain.service.profile.search;

import com.inxbtwt.domain.dto.profile.http.ProfileSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchProfilesUseCase {

    Page<ProfileSummaryResponse> searchProfiles(String query, Pageable pageable);
}
