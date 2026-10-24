package com.inxbtwt.infra.exception.http;

import com.inxbtwt.infra.controller.profile.ProfileController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ProfileController.class)
public class ProfileExceptionHandler extends UserExceptionHandler {
}
