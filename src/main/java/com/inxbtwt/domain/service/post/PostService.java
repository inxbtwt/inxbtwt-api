package com.inxbtwt.domain.service.post;

import com.inxbtwt.domain.dto.post.http.CreatePostRequest;
import com.inxbtwt.domain.dto.post.http.PostResponse;
import com.inxbtwt.domain.dto.post.http.PostPageResponse;

import java.util.UUID;
import com.inxbtwt.domain.enums.PostProfileTab;

public interface PostService {

    PostResponse createPost(CreatePostRequest request);

    PostResponse getPost(UUID postId);

    PostPageResponse getPostsByAuthor(UUID authorId, PostProfileTab tab, int page, int size);
}
