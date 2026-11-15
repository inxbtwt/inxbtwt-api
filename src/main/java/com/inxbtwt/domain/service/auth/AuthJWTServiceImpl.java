package com.inxbtwt.domain.service.auth;

import java.util.function.Supplier;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.jwt.JwtGenerateResponse;
import com.inxbtwt.domain.dto.jwt.LoginRequest;
import com.inxbtwt.domain.dto.jwt.RefreshTokenRequest;
import com.inxbtwt.infra.client.auth.AuthApiClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthJWTServiceImpl implements AuthService {

    private final AuthApiClient authApiClient;

    @Override
    public JwtGenerateResponse authenticate(LoginRequest request) {
        return request(() -> authApiClient.authenticate(request), "login",
                "E-mail, username ou senha inválidos.");
    }

    @Override
    public JwtGenerateResponse refresh(RefreshTokenRequest request) {
        return request(() -> authApiClient.refresh(request), "renovação de sessão",
                "Refresh token inválido ou expirado.");
    }

    @Override
    public void logout(String refreshToken) {
        try {
            authApiClient.logout(new RefreshTokenRequest(refreshToken));
            log.info("Logout encaminhado ao auth-api");
        } catch (FeignException exception) {
            log.error("Falha ao encaminhar o logout ao auth-api (HTTP {})", exception.status());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível concluir o logout no serviço de autenticação.", exception);
        }
    }

    private JwtGenerateResponse request(
            Supplier<ApiResponse<JwtGenerateResponse>> operation,
            String action,
            String unauthorizedMessage
    ) {
        try {
            var response = operation.get();
            if (response == null || response.data() == null) {
                log.error("Resposta inválida do auth-api na {}", action);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Resposta inválida do serviço de autenticação.");
            }
            log.info("Tokens emitidos pelo auth-api na {}", action);
            return response.data();
        } catch (FeignException exception) {
            if (exception.status() == HttpStatus.UNAUTHORIZED.value()
                    || exception.status() == HttpStatus.BAD_REQUEST.value()) {
                log.warn("Auth-api recusou a {} (HTTP {})", action, exception.status());
                throw new BadCredentialsException(unauthorizedMessage);
            }
            log.error("Auth-api indisponível na {} (HTTP {})", action, exception.status());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Serviço de autenticação indisponível.", exception);
        }
    }
}
