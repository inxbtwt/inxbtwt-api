package com.inxbtwt.infra.controller.auth;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.user.http.UserResponse;
import com.inxbtwt.domain.util.UserAuthUtil;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class MeController {

    private final UserAuthUtil userAuthUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        return ApiControllerUtils.ok("Usuário autenticado encontrado.", userAuthUtil.getUser());
    }
}
