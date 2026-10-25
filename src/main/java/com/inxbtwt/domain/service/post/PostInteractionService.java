package com.inxbtwt.domain.service.post;

import com.inxbtwt.domain.dto.post.http.CommentCreatedResponse;
import com.inxbtwt.domain.dto.post.http.CreatePostRequest;
import com.inxbtwt.domain.dto.post.http.PostPageResponse;
import com.inxbtwt.domain.dto.post.http.PostResponse;

import java.util.UUID;

public interface PostInteractionService {

    PostResponse setLike(UUID postId, boolean liked);

    PostResponse setRepost(UUID postId, boolean reposted);

    CommentCreatedResponse comment(UUID postId, CreatePostRequest request);

    PostPageResponse getComments(UUID postId, int page, int size);
}
