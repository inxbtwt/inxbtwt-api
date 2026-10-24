package com.inxbtwt.domain.service.profile;

import com.inxbtwt.db.entity.ProfileEntity;
import com.inxbtwt.db.repository.ProfileRepository;
import com.inxbtwt.db.repository.FriendshipRepository;
import com.inxbtwt.db.repository.UserDataRepository;
import com.inxbtwt.domain.dto.profile.http.PatchProfileRequest;
import com.inxbtwt.domain.dto.profile.http.ProfileResponse;
import com.inxbtwt.domain.dto.profile.http.ProfileSummaryResponse;
import com.inxbtwt.domain.dto.profile.http.ProfileViewResponse;
import com.inxbtwt.domain.service.user.find.FindUserUseCase;
import com.inxbtwt.domain.service.profile.search.SearchProfilesUseCase;
import com.inxbtwt.domain.util.UserAuthUtil;
import com.inxbtwt.domain.util.UserUtils;
import com.inxbtwt.infra.exception.domain.user.UserInvalidException;
import com.inxbtwt.infra.exception.domain.user.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final FriendshipRepository friendships;
    private final UserDataRepository userRepository;
    private final FindUserUseCase findUserUseCase;
    private final UserAuthUtil userAuthUtil;
    private final Validator validator;
    private final SearchProfilesUseCase searchProfilesUseCase;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile() {
        var userId = userAuthUtil.getUserId();
        return profileRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(() -> new ProfileResponse(null, userId, null, null, friendships.countFriends(userId)));
    }

    @Override
    public Page<ProfileSummaryResponse> searchProfiles(String query, Pageable pageable) {
        return searchProfilesUseCase.searchProfiles(query, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileViewResponse getProfileByUsername(String username) {
        userAuthUtil.requireAuthenticated();
        if (username == null || username.isBlank()) {
            throw new UserInvalidException("Informe o username do perfil.");
        }

        var user = userRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new UserNotFoundException("Perfil não encontrado."));
        var profile = profileRepository.findByUserId(user.getUserId()).orElse(null);
        String bio = profile == null ? null : profile.getBio();
        String coverUrl = profile == null || profile.getCoverUrl() == null
                ? user.getCoverUrl() : profile.getCoverUrl();

        return new ProfileViewResponse(user.getUserId(), user.getName(), user.getUsername(),
                bio, user.getPhotoUrl(), coverUrl, user.getCreatedAt(), friendships.countFriends(user.getUserId()));
    }

    @Override
    @Transactional
    public ProfileResponse patchProfile(PatchProfileRequest request) {
        UserUtils.validateFields(request, validator);
        if (request.bio() == null && request.coverUrl() == null) {
            throw new UserInvalidException("Informe a bio ou a URL da capa para atualizar o perfil.");
        }

        var userId = userAuthUtil.getUserId();
        var profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> ProfileEntity.builder()
                        .user_id(findUserUseCase.findUser(userId))
                        .build());

        if (request.bio() != null) {
            profile.setBio(request.bio());
        }
        if (request.coverUrl() != null) {
            String coverUrl = request.coverUrl().trim();
            profile.setCoverUrl(coverUrl.isEmpty() ? null : coverUrl);
        }

        return toResponse(profileRepository.saveAndFlush(profile));
    }

    private ProfileResponse toResponse(ProfileEntity profile) {
        return new ProfileResponse(profile.getId(), profile.getUser_id().getUserId(),
                profile.getBio(), profile.getCoverUrl(), friendships.countFriends(profile.getUser_id().getUserId()));
    }
}
