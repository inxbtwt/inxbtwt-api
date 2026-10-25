package com.inxbtwt.infra.exception.http;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.infra.controller.feed.FeedController;
import com.inxbtwt.infra.controller.post.PostController;
import com.inxbtwt.infra.controller.post.PostInteractionController;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import com.inxbtwt.infra.exception.domain.post.PostNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {PostController.class, PostInteractionController.class, FeedController.class})
public class PostExceptionHandler extends UserExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePostNotFound(PostNotFoundException exception) {
        return ApiControllerUtils.notFound(exception.getMessage());
    }
}
