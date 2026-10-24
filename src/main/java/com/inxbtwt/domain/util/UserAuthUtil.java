package com.inxbtwt.domain.util;

import com.inxbtwt.db.repository.UserDataRepository;
import com.inxbtwt.domain.dto.user.http.UserResponse;
import com.inxbtwt.domain.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserAuthUtil {

    private final UserDataRepository userRepository;
    private final UserMapper userMapper;

    public boolean isAuthenticated() {
        return isUserAuthentication(SecurityContextHolder.getContext().getAuthentication());
    }

    public void requireAuthenticated() {
        getUserId();
    }

    public UUID getUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!isUserAuthentication(authentication)) {
            throw new AuthenticationCredentialsNotFoundException("Autenticação obrigatória.");
        }

        return (UUID) authentication.getPrincipal();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser() {
        var user = userRepository.findByUserId(getUserId())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException(
                        "Usuário autenticado não encontrado."));

        return userMapper.toResponse(user);
    }

    private boolean isUserAuthentication(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getPrincipal() instanceof UUID;
    }
}
