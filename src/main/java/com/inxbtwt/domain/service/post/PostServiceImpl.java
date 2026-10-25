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
import com.inxbtwt.domain.dto.post.http.UpdatePostRequest;
import com.inxbtwt.domain.dto.post.http.PostDeletedResponse;
import com.inxbtwt.db.repository.PostMediaRepository;
import com.inxbtwt.infra.exception.http.ForbiddenHttpException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
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
    private final PostMediaService postMedia;
    private final PostMediaRepository mediaRepository;

    @Override
    @Transactional
    public PostResponse createPost(CreatePostRequest request) {
        return createPost(request == null ? null : request.content(), List.of(), null);
    }

    @Override
    @Transactional
    public PostResponse createPost(String content, List<MultipartFile> files, String tenor) {
        var userId = userAuthUtil.getUserId();
        var attachments = files == null ? List.<MultipartFile>of() : files;
        String tenorId = TenorEmbedParser.parse(tenor);
        if (tenorId != null && !attachments.isEmpty()) {
            throw new DomainException("Use um GIF do Tenor ou arquivos locais, sem misturar os dois.");
        }
        if ((content == null || content.isBlank()) && attachments.isEmpty() && tenorId == null) {
            throw new DomainException("Informe um texto, uma imagem ou um GIF para publicar.");
        }
        postMedia.validate(attachments);
        var post = PostEntity.builder()
                .author(findUserUseCase.findUser(userId))
                .content(content == null ? "" : content.strip())
                .build();
        postRepository.saveAndFlush(post);
        if (!attachments.isEmpty()) {
            postMedia.attach(post, attachments);
        }
        if (tenorId != null) {
            postMedia.attachTenor(post, tenorId);
        }
        return postMapper.toResponse(post);
    }

    @Override
    @Transactional
    public PostResponse updatePost(UUID postId, UpdatePostRequest request) {
        var post = findOwnedPost(postId);
        if (request == null || request.content() == null) {
            throw new DomainException("Informe o texto da publicação.");
        }
        String content = request.content().strip();
        if (content.isBlank() && mediaRepository.findByPost_IdInOrderByPositionAsc(List.of(post.getId())).isEmpty()) {
            throw new DomainException("Uma publicação sem mídia precisa ter texto.");
        }
        if (!content.equals(post.getContent())) {
            post.updateContent(content);
            postRepository.saveAndFlush(post);
        }
        return postMapper.toResponse(post);
    }

    @Override
    @Transactional
    public PostDeletedResponse deletePost(UUID postId) {
        var post = findOwnedPost(postId);
        post.remove();
        postRepository.saveAndFlush(post);
        postMedia.removeAttachments(post);
        var parent = post.getParent();
        return new PostDeletedResponse(postId,
                parent == null || parent.getDeletedAt() != null ? null : postMapper.toResponse(parent));
    }

    private PostEntity findOwnedPost(UUID postId) {
        var viewerId = userAuthUtil.getUserId();
        var post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException("Publicação não encontrada."));
        if (!post.getAuthor().getUserId().equals(viewerId)) {
            throw new ForbiddenHttpException("Você só pode editar ou excluir suas próprias publicações.");
        }
        return post;
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
            case REPLIES -> postRepository.findByAuthor_UserIdAndParentIsNotNullAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(authorId, pageable);
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
