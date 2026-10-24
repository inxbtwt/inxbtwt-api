package com.inxbtwt.domain.service.user.find;

import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.db.repository.UserDataRepository;
import com.inxbtwt.domain.util.UserUtils;
import com.inxbtwt.infra.exception.domain.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindUser implements FindUserUseCase{

    private final UserDataRepository userRepository;

    @Override
    public UserDataEntity findUser(UUID userId) {
        UserUtils.validateUserId(userId);
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));
    }
}
