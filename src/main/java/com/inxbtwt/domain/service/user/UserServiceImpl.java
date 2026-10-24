package com.inxbtwt.domain.service.user;

import com.inxbtwt.db.repository.UserDataRepository;
import com.inxbtwt.domain.dto.user.http.CreateUserRequest;
import com.inxbtwt.domain.dto.user.http.UserResponse;
import com.inxbtwt.domain.dto.user.http.UpdateUserRequest;
import com.inxbtwt.domain.dto.user.http.PatchUserRequest;
import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.infra.exception.domain.user.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import com.inxbtwt.domain.mapper.user.UserMapper;
import com.inxbtwt.domain.util.UserUtils;
import com.inxbtwt.infra.exception.domain.user.UserException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.inxbtwt.domain.service.bucket.BucketService;
import com.inxbtwt.domain.dto.bucket.UploadFileRequest;
import com.inxbtwt.domain.dto.bucket.UploadFileResponse;
import com.inxbtwt.infra.properties.BucketUrlsProperties;
import org.springframework.web.multipart.MultipartFile;
import com.inxbtwt.domain.dto.bucket.DownloadFileResponse;
import com.inxbtwt.domain.dto.user.http.UserPhotoUrlsResponse;
import com.inxbtwt.domain.enums.UserPhotoType;
import com.inxbtwt.infra.exception.domain.user.UserInvalidException;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDataRepository userRepository;
    private final UserMapper userMapper;
    private final Validator validator;
    private final BucketService bucketService;
    private final BucketUrlsProperties bucketUrls;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        return userMapper.toResponse(findUser(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        var user = findUser(userId);
        return saveUpdate(user, request);
    }

    @Override
    @Transactional
    public UserResponse patchUser(UUID userId, PatchUserRequest request) {
        var user = findUser(userId);
        UserUtils.validatePatch(request);
        var merged = new UpdateUserRequest(
                request.name() != null ? request.name() : user.getName(),
                request.email() != null ? request.email() : user.getEmail(),
                request.phone() != null ? request.phone() : user.getPhone(),
                request.password() != null ? request.password() : user.getPassword());
        return saveUpdate(user, merged);
    }

    private UserDataEntity findUser(UUID userId) {
        UserUtils.validateUserId(userId);
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));
    }

    private UserResponse saveUpdate(UserDataEntity user, UpdateUserRequest request) {
        UserUtils.validateFields(request, validator);
        UserUtils.validateEmailAvailability(userRepository.existsByEmailIgnoreCaseAndUserIdNot(request.email(), user.getUserId()));
        var updated = userMapper.updateEntity(user, request);
        if (!request.password().equals(user.getPassword())) {
            updated = updated.toBuilder().password(passwordEncoder.encode(request.password())).build();
        }
        return userMapper.toResponse(userRepository.saveAndFlush(updated));
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) throws UserException {
        log.info("m=createUser");
        UserUtils.validateFields(createUserRequest, validator);
        UserUtils.validateEmailAvailability(userRepository.existsByEmailIgnoreCase(createUserRequest.email()));
        var user = userMapper.toEntity(createUserRequest).toBuilder()
                .password(passwordEncoder.encode(createUserRequest.password())).build();
        userMapper.linkAddresses(user);
        var savedUser = userRepository.saveAndFlush(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UploadFileResponse uploadPhoto(UUID userId, MultipartFile photo, UserPhotoType type) {
        var user = findUser(userId);
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

    @Override
    @Transactional(readOnly = true)
    public UserPhotoUrlsResponse getPhotoUrls(UUID userId) {
        var user = findUser(userId);
        return new UserPhotoUrlsResponse(user.getUserId(), user.getPhotoUrl(), user.getCoverUrl());
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadFileResponse downloadPhoto(UUID userId, String url) {
        var user = findUser(userId);
        if (url == null || url.isBlank()) {
            throw new UserInvalidException("A URL da foto é obrigatória.");
        }
        if (!url.equals(user.getPhotoUrl()) && !url.equals(user.getCoverUrl())) {
            throw new UserNotFoundException("Foto não encontrada para este usuário.");
        }
        return bucketService.download(url);
    }

}
