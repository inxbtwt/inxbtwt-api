package com.inxbtwt.domain.service.user.userBlob;

import com.inxbtwt.db.entity.UserDataEntity;
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
    private final BucketService bucketService;
    private final BucketUrlsProperties bucketUrls;
    private final FindUserUseCase findUserUseCase;

    @Override
    @Transactional(readOnly = true)
    public UserPhotoUrlsResponse getPhotoUrls(UUID userId) {
        var user = findUserUseCase.findUser(userId);
        return new UserPhotoUrlsResponse(user.getUserId(), user.getPhotoUrl(), user.getCoverUrl());
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadFileResponse downloadPhoto(UUID userId, String url) {
        var user = findUserUseCase.findUser(userId);
        if (url == null || url.isBlank()) {
            throw new UserInvalidException("A URL da foto é obrigatória.");
        }
        if (!url.equals(user.getPhotoUrl()) && !url.equals(user.getCoverUrl())) {
            throw new UserNotFoundException("Foto não encontrada para este usuário.");
        }
        return bucketService.download(url);
    }

    @Override
    public UploadFileResponse uploadPhoto(UUID userId, MultipartFile photo, UserPhotoType type) {
        var user = findUserUseCase.findUser(userId);
        UserUtils.validatePhoto(photo);
        if (type == null) {
            throw new UserInvalidException("O tipo da foto é obrigatório.");
        }
        String path = type == UserPhotoType.COVER ? bucketUrls.coverProfile() : bucketUrls.photoProfile();
        var uploaded = bucketService.upload(new UploadFileRequest(photo, path));
        var updated = user.toBuilder();
        if (type == UserPhotoType.COVER) {
            updated.coverUrl(uploaded.url());
        } else {
            updated.photoUrl(uploaded.url());
        }
        userRepository.saveAndFlush(updated.build());
        return uploaded;
    }
}
