package com.inxbtwt.domain.mapper.post;

import com.inxbtwt.db.entity.PostEntity;
import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.db.repository.PostLikeRepository;
import com.inxbtwt.db.repository.PostRepostRepository;
import com.inxbtwt.db.repository.PostRepository;
import com.inxbtwt.db.repository.projection.PostInteractionCount;
import com.inxbtwt.domain.dto.post.http.*;
import com.inxbtwt.domain.util.UserAuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final PostLikeRepository likes;
    private final PostRepostRepository reposts;
    private final PostRepository posts;
    private final UserAuthUtil auth;

    public PostResponse toResponse(PostEntity post) {
        return toResponses(List.of(post)).getFirst();
    }

    /** Carrega contagens e estados do solicitante em lote, sem consultas por publicação. */
    public List<PostResponse> toResponses(List<PostEntity> publications) {
        if (publications.isEmpty()) {
            return List.of();
        }
        var viewerId = auth.getUserId();
        var ids = publications.stream().map(PostEntity::getId).toList();
        var likeCounts = counts(likes.countForPosts(ids));
        var repostCounts = counts(reposts.countForPosts(ids));
        var commentCounts = counts(posts.countComments(ids));
        var liked = new HashSet<>(likes.findViewerPostIds(viewerId, ids));
        var reposted = new HashSet<>(reposts.findViewerPostIds(viewerId, ids));
        return publications.stream().map(post -> {
            var parent = post.getParent();
            var reference = parent == null ? null : new PostReferenceResponse(parent.getPostId(),
                    parent.getContent(), author(parent.getAuthor()));
            return new PostResponse(post.getPostId(), post.getContent(), author(post.getAuthor()),
                    post.getCreatedAt(), post.getUpdatedAt(), likeCounts.getOrDefault(post.getId(), 0L),
                    repostCounts.getOrDefault(post.getId(), 0L), commentCounts.getOrDefault(post.getId(), 0L),
                    liked.contains(post.getId()), reposted.contains(post.getId()), reference, null, post.getCreatedAt());
        }).toList();
    }

    public PostAuthorResponse author(UserDataEntity user) {
        return new PostAuthorResponse(user.getUserId(), user.getName(), user.getUsername(), user.getPhotoUrl());
    }

    private Map<Long, Long> counts(List<PostInteractionCount> values) {
        return values.stream().collect(Collectors.toMap(PostInteractionCount::getPostId, PostInteractionCount::getTotal));
    }
}
