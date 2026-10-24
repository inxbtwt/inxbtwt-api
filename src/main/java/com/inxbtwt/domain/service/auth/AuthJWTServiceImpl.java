package com.inxbtwt.domain.service.auth;
import com.inxbtwt.db.entity.RefreshTokenEntity;
import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.db.repository.*;
import com.inxbtwt.domain.dto.jwt.*;
import com.inxbtwt.domain.dto.user.http.UserResponse;
import com.inxbtwt.domain.mapper.user.UserMapper;
import com.inxbtwt.domain.util.JwtUtils;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthJWTServiceImpl implements AuthService {
    private final UserDataRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwt;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public JwtGenerateResponse authenticate(LoginRequest request) {
        var user = users.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos."));
        var stored = user.getPassword();
        boolean legacy = !stored.startsWith("{");
        boolean matches = legacy
                ? MessageDigest.isEqual(stored.getBytes(StandardCharsets.UTF_8), request.password().getBytes(StandardCharsets.UTF_8))
                : passwordEncoder.matches(request.password(), stored);
        if (!matches) throw new BadCredentialsException("E-mail ou senha inválidos.");
        if (legacy || passwordEncoder.upgradeEncoding(stored)) {
            users.save(user.toBuilder().password(passwordEncoder.encode(request.password())).build());
        }
        return issue(user);
    }

    @Override
    @Transactional
    public JwtGenerateResponse refresh(RefreshTokenRequest request) {
        UUID tokenId;
        UUID userId;
        try {
            var claims = jwt.decode(request.refreshToken());
            if (!"refresh".equals(claims.get("token_type", String.class))) {
                throw new JwtException("Tipo inválido.");
            }
            tokenId = UUID.fromString(claims.getId());
            userId = UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BadCredentialsException("Refresh token inválido ou expirado.");
        }
        if (refreshTokens.consume(tokenId, userId, Instant.now()) != 1) {
            throw new BadCredentialsException("Refresh token inválido ou já utilizado.");
        }
        var user = users.findByUserId(userId)
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado."));
        return issue(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        UUID tokenId;
        UUID userId;

        try {
            var claims = jwt.decode(refreshToken);

            if (!"refresh".equals(claims.get("token_type", String.class))) {
                return;
            }

            tokenId = UUID.fromString(claims.getId());
            userId = UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException exception) {
            // Invalid or expired cookies must not prevent local logout.
            return;
        }

        refreshTokens.revoke(tokenId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me(UUID userId) {
        return userMapper.toResponse(users.findByUserId(userId)
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado.")));
    }

    private JwtGenerateResponse issue(UserDataEntity user) {
        var response = jwt.generate(user.getUserId(), List.of("USER"));
        var claims = jwt.decode(response.refreshToken());
        refreshTokens.save(new RefreshTokenEntity(UUID.fromString(claims.getId()), user.getUserId(),
                claims.getExpiration().toInstant()));
        return response;
    }
}
