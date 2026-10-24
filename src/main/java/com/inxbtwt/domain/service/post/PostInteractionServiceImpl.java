package com.inxbtwt.domain.service.post;

import com.inxbtwt.db.entity.PostEntity;
import com.inxbtwt.db.repository.PostLikeRepository;
import com.inxbtwt.db.repository.PostRepostRepository;
import com.inxbtwt.db.repository.PostRepository;
import com.inxbtwt.domain.dto.post.http.*;
import com.inxbtwt.domain.mapper.post.PostMapper;
import com.inxbtwt.domain.service.user.find.FindUserUseCase;
import com.inxbtwt.domain.util.UserAuthUtil;
import com.inxbtwt.infra.exception.domain.DomainException;
import com.inxbtwt.infra.exception.domain.post.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostInteractionServiceImpl implements PostInteractionService {

    private final PostRepository posts;
    private final PostLikeRepository likes;
    private final PostRepostRepository reposts;
    private final FindUserUseCase findUser;
    private final UserAuthUtil auth;
    private final PostMapper mapper;

    @Override
    @Transactional
    public PostResponse setLike(UUID postId, boolean liked) {
        var user = findUser.findUser(auth.getUserId());
        var post = findPost(postId);
        if (liked) {
            likes.add(post.getId(), user.getId());
        } else {
            likes.remove(post.getId(), user.getId());
        }
        return mapper.toResponse(post);
    }

    @Override
    @Transactional
    public PostResponse setRepost(UUID postId, boolean reposted) {
        var user = findUser.findUser(auth.getUserId());
        var post = findPost(postId);
        if (reposted) {
            reposts.add(post.getId(), user.getId());
        } else {
            reposts.remove(post.getId(), user.getId());
        }
        return mapper.toResponse(post);
    }

    @Override
    @Transactional
    public CommentCreatedResponse comment(UUID postId, CreatePostRequest request) {
        var user = findUser.findUser(auth.getUserId());
        var parent = findPost(postId);
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new DomainException("O comentário não pode estar vazio.");
        }
        var comment = PostEntity.builder().author(user).parent(parent)
                .content(request.content().strip()).build();
        posts.saveAndFlush(comment);
        return new CommentCreatedResponse(mapper.toResponse(comment), mapper.toResponse(parent));
    }

    @Override
    @Transactional(readOnly = true)
    public PostPageResponse getComments(UUID postId, int page, int size) {
        auth.requireAuthenticated();
        if (page < 0 || size < 1 || size > 100) {
            throw new DomainException("Informe página não negativa e tamanho entre 1 e 100.");
        }
        findPost(postId);
        var comments = posts.findByParent_PostIdOrderByCreatedAtDescIdDesc(postId, PageRequest.of(page, size));
        return new PostPageResponse(mapper.toResponses(comments.getContent()), comments.getNumber(),
                comments.getSize(), comments.hasNext(), comments.hasPrevious());
    }

    private PostEntity findPost(UUID postId) {
        return posts.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException("Publicação não encontrada."));
    }
}
