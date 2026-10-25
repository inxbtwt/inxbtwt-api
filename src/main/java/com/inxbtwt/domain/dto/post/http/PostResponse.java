package com.inxbtwt.domain.dto.post.http;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

public record PostResponse(
        UUID postId,
        String content,
        PostAuthorResponse author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long likeCount,
        long repostCount,
        long commentCount,
        boolean liked,
        boolean reposted,
        PostReferenceResponse parent,
        PostAuthorResponse repostedBy,
        List<PostMediaResponse> media,
        LocalDateTime activityAt
) {

    public PostResponse asRepost(PostAuthorResponse user, LocalDateTime repostedAt) {
        return new PostResponse(postId, content, author, createdAt, updatedAt, likeCount,
                repostCount, commentCount, liked, reposted, parent, user, media, repostedAt);
    }
}
