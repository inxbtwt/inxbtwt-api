package com.inxbtwt.infra.controller.post;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.post.http.CreatePostRequest;
import com.inxbtwt.domain.dto.post.http.PostResponse;
import com.inxbtwt.domain.dto.post.http.PostPageResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.inxbtwt.domain.service.post.PostService;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import com.inxbtwt.infra.exception.http.HttpMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import com.inxbtwt.domain.dto.post.http.UpdatePostRequest;
import com.inxbtwt.domain.dto.post.http.PostDeletedResponse;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.CacheControl;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import com.inxbtwt.domain.service.post.PostMediaService;
import com.inxbtwt.domain.enums.PostProfileTab;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostMediaService postMedia;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ApiControllerUtils.created(HttpMessages.POST_CREATED, postService.createPost(request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostResponse>> createPostWithMedia(
            @RequestParam(required = false) String content,
            @RequestPart(value = "media", required = false) List<MultipartFile> media,
            @RequestParam(required = false) String tenor) {
        return ApiControllerUtils.created(HttpMessages.POST_CREATED, postService.createPost(content, media, tenor));
    }

    @GetMapping("/{postId}/media/{mediaId}")
    public ResponseEntity<byte[]> downloadMedia(@PathVariable UUID postId, @PathVariable UUID mediaId) {
        var file = postMedia.download(postId, mediaId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.size())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .body(file.data());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PostPageResponse>> getPostsByAuthor(
            @RequestParam UUID authorId,
            @RequestParam(defaultValue = "PUBLICATIONS") PostProfileTab tab,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiControllerUtils.ok(HttpMessages.POSTS_FOUND,
                postService.getPostsByAuthor(authorId, tab, page, size));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable UUID postId, @Valid @RequestBody UpdatePostRequest request) {
        return ApiControllerUtils.ok("Publicação atualizada.", postService.updatePost(postId, request));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDeletedResponse>> deletePost(@PathVariable UUID postId) {
        return ApiControllerUtils.ok("Publicação removida.", postService.deletePost(postId));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable UUID postId) {
        return ApiControllerUtils.ok(HttpMessages.POST_FOUND, postService.getPost(postId));
    }
}
