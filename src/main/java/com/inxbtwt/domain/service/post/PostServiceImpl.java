package com.inxbtwt.domain.service.post;

import com.inxbtwt.db.entity.PostEntity;
import com.inxbtwt.db.repository.PostRepository;
import com.inxbtwt.domain.dto.post.http.CreatePostRequest;
import com.inxbtwt.domain.dto.post.http.PostResponse;
import com.inxbtwt.domain.dto.post.http.PostPageResponse;
import org.springframework.data.domain.PageRequest;
import com.inxbtwt.domain.mapper.post.PostMapper;
import com.inxbtwt.domain.service.user.find.FindUserUseCase;
import com.inxbtwt.domain.util.UserAuthUtil;
import com.inxbtwt.infra.exception.domain.DomainException;
import com.inxbtwt.infra.exception.domain.post.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.HashMap;
import com.inxbtwt.domain.enums.PostProfileTab;
import com.inxbtwt.db.repository.PostRepostRepository;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final FindUserUseCase findUserUseCase;
    private final UserAuthUtil userAuthUtil;
    private final PostMapper postMapper;
    private final PostRepostRepository repostRepository;

    @Override
    @Transactional
    public PostResponse createPost(CreatePostRequest request) {
        var userId = userAuthUtil.getUserId();
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new DomainException("O conteúdo da publicação é obrigatório.");
        }

        var post = PostEntity.builder()
                .author(findUserUseCase.findUser(userId))
                .content(request.content().strip())
                .build();
        return postMapper.toResponse(postRepository.saveAndFlush(post));
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPost(UUID postId) {
        userAuthUtil.requireAuthenticated();
        if (postId == null) {
            throw new DomainException("O identificador da publicação é obrigatório.");
        }
        var post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException("Publicação não encontrada."));
        return postMapper.toResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostPageResponse getPostsByAuthor(UUID authorId, PostProfileTab tab, int page, int size) {
        userAuthUtil.requireAuthenticated();
        if (page < 0 || size < 1 || size > 100) {
            throw new DomainException("Informe página não negativa e tamanho entre 1 e 100.");
        }
        var author = findUserUseCase.findUser(authorId);
        var pageable = PageRequest.of(page, size);
        var posts = switch (tab) {
            case PUBLICATIONS -> postRepository.findPublications(authorId, pageable);
            case REPLIES -> postRepository.findByAuthor_UserIdAndParentIsNotNullOrderByCreatedAtDescIdDesc(authorId, pageable);
            case LIKES -> postRepository.findLikedPosts(authorId, pageable);
        };
        var content = postMapper.toResponses(posts.getContent());
        if (tab == PostProfileTab.PUBLICATIONS && !content.isEmpty()) {
            var profileReposts = new HashMap<UUID, java.time.LocalDateTime>();
            var ids = posts.getContent().stream().map(PostEntity::getId).toList();
            repostRepository.findProfileReposts(authorId, ids).forEach(repost ->
                    profileReposts.put(repost.getPost().getPostId(), repost.getCreatedAt()));
            content = content.stream().map(post -> profileReposts.containsKey(post.postId())
                    ? post.asRepost(postMapper.author(author), profileReposts.get(post.postId())) : post).toList();
        }
        return new PostPageResponse(content, posts.getNumber(), posts.getSize(),
                posts.hasNext(), posts.hasPrevious());
    }

}
