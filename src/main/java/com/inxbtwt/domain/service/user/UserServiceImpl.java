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


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDataRepository userRepository;
    private final UserMapper userMapper;
    private final Validator validator;

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
                request.photoUrl() != null ? request.photoUrl() : user.getPhotoUrl(),
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
        return userMapper.toResponse(userRepository.saveAndFlush(updated));
    }
    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) throws UserException {
        log.info("m=createUser");
        UserUtils.validateFields(createUserRequest, validator);
        UserUtils.validateEmailAvailability(userRepository.existsByEmailIgnoreCase(createUserRequest.email()));
        var user = userMapper.toEntity(createUserRequest);
        var savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

}
