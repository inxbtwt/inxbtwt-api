package com.inxbtwt.domain.service.post;

import com.inxbtwt.db.entity.PostEntity;
import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.domain.dto.notification.NotificationCommand;
import com.inxbtwt.domain.enums.NotificationType;
import com.inxbtwt.db.repository.PostLikeRepository;
import com.inxbtwt.db.repository.PostRepostRepository;
import com.inxbtwt.db.repository.PostRepository;
import com.inxbtwt.domain.dto.post.http.*;
import com.inxbtwt.domain.mapper.post.PostMapper;
import com.inxbtwt.domain.service.notification.NotificationService;
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
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PostResponse setLike(UUID postId, boolean liked) {
        var user = findUser.findUser(auth.getUserId());
        var post = findPost(postId);
        if (liked) {
            if (likes.add(post.getId(), user.getId()) > 0) {
                notifyInteraction(post, user, NotificationType.POST_LIKED);
            }
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
            if (reposts.add(post.getId(), user.getId()) > 0) {
                notifyInteraction(post, user, NotificationType.POST_REPOSTED);
            }
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
        notifyInteraction(parent, user, NotificationType.POST_COMMENTED);
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
        var comments = posts.findByParent_PostIdAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(postId, PageRequest.of(page, size));
        return new PostPageResponse(mapper.toResponses(comments.getContent()), comments.getNumber(),
                comments.getSize(), comments.hasNext(), comments.hasPrevious());
    }

    /** A notificação do site participa da mesma transação da interação, evitando entregas de ações revertidas. */
    private void notifyInteraction(PostEntity post, UserDataEntity actor, NotificationType type) {
        var recipientId = post.getAuthor().getUserId();
        if (recipientId.equals(actor.getUserId())) {
            return;
        }
        String title;
        String action;
        switch (type) {
            case POST_LIKED -> {
                title = "Nova curtida";
                action = "curtiu sua publicação.";
            }
            case POST_REPOSTED -> {
                title = "Novo repost";
                action = "repostou sua publicação.";
            }
            case POST_COMMENTED -> {
                title = "Novo comentário";
                action = "comentou na sua publicação.";
            }
            default -> throw new IllegalArgumentException("Tipo de interação não suportado: " + type);
        }
        notificationService.send(new NotificationCommand(recipientId, actor.getUserId(), post.getPostId(), type, title, "@" + actor.getUsername() + " " + action));
    }

    private PostEntity findPost(UUID postId) {
        return posts.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException("Publicação não encontrada."));
    }
}
