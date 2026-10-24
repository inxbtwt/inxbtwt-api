package com.inxbtwt.domain.service.profile;

import com.inxbtwt.db.entity.ProfileEntity;
import com.inxbtwt.db.repository.ProfileRepository;
import com.inxbtwt.domain.dto.profile.http.PatchProfileRequest;
import com.inxbtwt.domain.dto.profile.http.ProfileResponse;
import com.inxbtwt.domain.service.user.find.FindUserUseCase;
import com.inxbtwt.domain.util.UserAuthUtil;
import com.inxbtwt.domain.util.UserUtils;
import com.inxbtwt.infra.exception.domain.user.UserInvalidException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final FindUserUseCase findUserUseCase;
    private final UserAuthUtil userAuthUtil;
    private final Validator validator;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile() {
        var userId = userAuthUtil.getUserId();
        return profileRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(() -> new ProfileResponse(null, userId, null, null));
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
                profile.getBio(), profile.getCoverUrl());
    }
}
