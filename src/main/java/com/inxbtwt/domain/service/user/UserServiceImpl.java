package com.inxbtwt.domain.service.user;

import com.inxbtwt.db.repository.UserDataRepository;
import com.inxbtwt.domain.dto.user.http.CreateUserRequest;
import com.inxbtwt.domain.dto.user.http.UserResponse;
import com.inxbtwt.domain.dto.user.http.UpdateUserRequest;
import com.inxbtwt.domain.dto.user.http.PatchUserRequest;
import com.inxbtwt.domain.dto.user.http.FieldAvailabilityResponse;
import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.domain.service.user.find.FindUserUseCase;
import com.inxbtwt.domain.service.user.userBlob.UserBlobService;
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
    private final PasswordEncoder passwordEncoder;
    private final UserBlobService userBlobService;
    private final FindUserUseCase findUserUseCase;

    @Override
    @Transactional(readOnly = true)
    public FieldAvailabilityResponse getAvailability(String field, String value) {
        if (field == null || field.isBlank()) {
            throw new UserInvalidException("Informe o campo: username, email ou phone.");
        }
        if (value == null || value.isBlank()) {
            throw new UserInvalidException("O valor para consulta é obrigatório.");
        }

        String normalizedValue = value.trim();
        boolean exists = switch (field.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "username" -> userRepository.existsByUsernameIgnoreCase(normalizedValue);
            case "email" -> userRepository.existsByEmailIgnoreCase(normalizedValue);
            case "phone" -> userRepository.existsByNormalizedPhone(UserUtils.normalizePhone(normalizedValue));
            default -> throw new UserInvalidException("Campo inválido. Utilize username, email ou phone.");
        };
        return new FieldAvailabilityResponse(!exists);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        return userMapper.toResponse(findUserUseCase.findUser(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        var user = findUserUseCase.findUser(userId);
        return saveUpdate(user, request);
    }

    @Override
    @Transactional
    public UserResponse patchUser(UUID userId, PatchUserRequest request) {
        var user = findUserUseCase.findUser(userId);
        UserUtils.validatePatch(request);

        var builder = userMapper.toUpdateRequest(user).toBuilder();
        userMapper.mergePatch(request, builder);

        return saveUpdate(user, builder.build());
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) throws UserException {
        log.info("m=createUser");
        UserUtils.validateFields(createUserRequest, validator);
        UserUtils.validateEmailAvailability(userRepository.existsByEmailIgnoreCase(createUserRequest.email()));
        UserUtils.validateUsernameAvailability(userRepository.existsByUsernameIgnoreCase(createUserRequest.username()));
        UserUtils.validatePhoneAvailability(userRepository.existsByNormalizedPhone(UserUtils.normalizePhone(createUserRequest.phone())));
        var user = userMapper.toEntity(createUserRequest).toBuilder()
                .password(passwordEncoder.encode(createUserRequest.password())).build();
        userMapper.linkAddresses(user);
        var savedUser = userRepository.saveAndFlush(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UploadFileResponse uploadPhoto(UUID userId, MultipartFile photo, UserPhotoType type) {
        return userBlobService.uploadPhoto(userId, photo, type);
    }

    @Override
    public UserPhotoUrlsResponse getPhotoUrls(UUID userId) {
        return userBlobService.getPhotoUrls(userId);
    }

    @Override
    public DownloadFileResponse downloadPhoto(UUID userId, String url) {
        return userBlobService.downloadPhoto(userId, url);
    }

    private UserResponse saveUpdate(UserDataEntity user, UpdateUserRequest request) {
        UserUtils.validateFields(request, validator);
        UserUtils.validateEmailAvailability(userRepository.existsByEmailIgnoreCaseAndUserIdNot(request.email(), user.getUserId()));
        UserUtils.validateUsernameAvailability(userRepository.existsByUsernameIgnoreCaseAndUserIdNot(request.username(), user.getUserId()));
        UserUtils.validatePhoneAvailability(userRepository.existsByNormalizedPhoneAndUserIdNot(
                UserUtils.normalizePhone(request.phone()), user.getUserId()));
        var updated = userMapper.updateEntity(user, request);
        if (!request.password().equals(user.getPassword())) {
            updated = updated.toBuilder().password(passwordEncoder.encode(request.password())).build();
        }
        return userMapper.toResponse(userRepository.saveAndFlush(updated));
    }

}
