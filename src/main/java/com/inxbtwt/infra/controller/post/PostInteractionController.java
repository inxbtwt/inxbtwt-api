package com.inxbtwt.infra.controller.post;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.post.http.*;
import com.inxbtwt.domain.service.post.PostInteractionService;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/posts/{postId}")
@RequiredArgsConstructor
public class PostInteractionController {

    private final PostInteractionService interactions;

    @PutMapping("/like")
    public ResponseEntity<ApiResponse<PostResponse>> like(@PathVariable UUID postId) {
        return ApiControllerUtils.ok("Publicação curtida.", interactions.setLike(postId, true));
    }

    @DeleteMapping("/like")
    public ResponseEntity<ApiResponse<PostResponse>> unlike(@PathVariable UUID postId) {
        return ApiControllerUtils.ok("Curtida removida.", interactions.setLike(postId, false));
    }

    @PutMapping("/repost")
    public ResponseEntity<ApiResponse<PostResponse>> repost(@PathVariable UUID postId) {
        return ApiControllerUtils.ok("Publicação repostada.", interactions.setRepost(postId, true));
    }

    @DeleteMapping("/repost")
    public ResponseEntity<ApiResponse<PostResponse>> undoRepost(@PathVariable UUID postId) {
        return ApiControllerUtils.ok("Repost removido.", interactions.setRepost(postId, false));
    }

    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<CommentCreatedResponse>> comment(
            @PathVariable UUID postId, @Valid @RequestBody CreatePostRequest request) {
        return ApiControllerUtils.created("Comentário publicado.", interactions.comment(postId, request));
    }

    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<PostPageResponse>> getComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiControllerUtils.ok("Comentários consultados.", interactions.getComments(postId, page, size));
    }
}
