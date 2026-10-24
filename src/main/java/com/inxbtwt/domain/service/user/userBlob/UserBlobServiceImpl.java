package com.inxbtwt.domain.service.user.userBlob;

import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.db.entity.ProfileEntity;
import com.inxbtwt.db.repository.ProfileRepository;
import com.inxbtwt.db.repository.UserDataRepository;
import com.inxbtwt.domain.dto.bucket.DownloadFileResponse;
import com.inxbtwt.domain.dto.bucket.UploadFileRequest;
import com.inxbtwt.domain.dto.bucket.UploadFileResponse;
import com.inxbtwt.domain.dto.user.http.UserPhotoUrlsResponse;
import com.inxbtwt.domain.enums.UserPhotoType;
import com.inxbtwt.domain.service.bucket.BucketService;
import com.inxbtwt.domain.service.user.find.FindUserUseCase;
import com.inxbtwt.domain.util.UserUtils;
import com.inxbtwt.infra.exception.domain.user.UserInvalidException;
import com.inxbtwt.infra.exception.domain.user.UserNotFoundException;
import com.inxbtwt.infra.properties.BucketUrlsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBlobServiceImpl implements UserBlobService {

    private final UserDataRepository userRepository;
    private final ProfileRepository profileRepository;
    private final BucketService bucketService;
    private final BucketUrlsProperties bucketUrls;
    private final FindUserUseCase findUserUseCase;

    @Override
    @Transactional(readOnly = true)
    public UserPhotoUrlsResponse getPhotoUrls(UUID userId) {
        var user = findUserUseCase.findUser(userId);
        String coverUrl = profileRepository.findByUserId(userId)
                .map(ProfileEntity::getCoverUrl)
                .orElse(user.getCoverUrl());
        return new UserPhotoUrlsResponse(user.getUserId(), user.getPhotoUrl(), coverUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadFileResponse downloadPhoto(UUID userId, String url) {
        var user = findUserUseCase.findUser(userId);
        if (url == null || url.isBlank()) {
            throw new UserInvalidException("A URL da foto é obrigatória.");
        }
        String profileCoverUrl = profileRepository.findByUserId(userId)
                .map(ProfileEntity::getCoverUrl)
                .orElse(null);
        if (!url.equals(user.getPhotoUrl()) && !url.equals(user.getCoverUrl()) && !url.equals(profileCoverUrl)) {
            throw new UserNotFoundException("Foto não encontrada para este usuário.");
        }
        return bucketService.download(url);
    }

    @Override
    @Transactional
    public UploadFileResponse uploadPhoto(UUID userId, MultipartFile photo, UserPhotoType type) {
        var user = findUserUseCase.findUser(userId);
        UserUtils.validatePhoto(photo);
        if (type == null) {
            throw new UserInvalidException("O tipo da foto é obrigatório.");
        }
        String path = type == UserPhotoType.COVER ? bucketUrls.coverProfile() : bucketUrls.photoProfile();
        var uploaded = bucketService.upload(new UploadFileRequest(photo, path));
        if (type == UserPhotoType.COVER) {
            var profile = profileRepository.findByUserId(userId)
                    .orElseGet(() -> ProfileEntity.builder().user_id(user).build());
            profile.setCoverUrl(uploaded.url());
            profileRepository.saveAndFlush(profile);
        } else {
            userRepository.saveAndFlush(user.toBuilder().photoUrl(uploaded.url()).build());
        }
        return uploaded;
    }
}
