package com.inxbtwt.domain.service.post;

import com.inxbtwt.db.entity.PostEntity;
import com.inxbtwt.db.entity.PostMediaEntity;
import com.inxbtwt.db.repository.PostMediaRepository;
import com.inxbtwt.domain.dto.bucket.DownloadFileResponse;
import com.inxbtwt.domain.dto.bucket.UploadFileRequest;
import com.inxbtwt.domain.service.bucket.BucketService;
import com.inxbtwt.domain.util.UserAuthUtil;
import com.inxbtwt.infra.exception.domain.DomainException;
import com.inxbtwt.infra.exception.domain.post.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostMediaService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final long MAX_PIXELS = 40_000_000;
    private static final Map<String, String> FORMATS = Map.of(
            "png", "image/png", "jpeg", "image/jpeg", "gif", "image/gif");

    private final PostMediaRepository media;
    private final BucketService bucket;
    private final UserAuthUtil auth;

    /** Valida todos os anexos antes de qualquer upload, preservando os bytes e a animação dos GIFs. */
    public void validate(List<MultipartFile> files) {
        if (files.size() > 4) {
            throw new DomainException("Envie até 4 imagens ou 1 GIF por publicação.");
        }
        boolean hasGif = false;
        for (var file : files) {
            if (file == null || file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
                throw new DomainException("Cada arquivo deve ter conteúdo e no máximo 10 MB.");
            }
            String type = validateImage(file);
            if ("image/gif".equals(type)) {
                hasGif = true;
            }
        }
        if (hasGif && files.size() > 1) {
            throw new DomainException("Publique um GIF sozinho, sem outras imagens.");
        }
    }

    private String validateImage(MultipartFile file) {
        try (var stream = file.getInputStream(); var image = ImageIO.createImageInputStream(stream)) {
            if (image == null) {
                throw new DomainException("Arquivo de imagem inválido.");
            }
            var readers = ImageIO.getImageReaders(image);
            if (!readers.hasNext()) {
                throw new DomainException("Envie uma imagem PNG, JPEG ou GIF válida.");
            }
            var reader = readers.next();
            try {
                reader.setInput(image, true, true);
                String type = FORMATS.get(reader.getFormatName().toLowerCase(Locale.ROOT));
                if (type == null || !type.equals(file.getContentType())) {
                    throw new DomainException("O conteúdo do arquivo deve corresponder ao formato PNG, JPEG ou GIF informado.");
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1 || (long) width * height > MAX_PIXELS) {
                    throw new DomainException("A imagem deve ter no máximo 40 megapixels.");
                }
                // Lê o primeiro quadro para rejeitar arquivos truncados sem regravar GIFs animados.
                reader.read(0);
                return type;
            } finally {
                reader.dispose();
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw new DomainException("Não foi possível ler a imagem. Envie um PNG, JPEG ou GIF válido.");
        }
    }

    public void attach(PostEntity post, List<MultipartFile> files) {
        var uploadedKeys = new ArrayList<String>();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    for (String key : uploadedKeys) {
                        try {
                            bucket.delete(key);
                        } catch (RuntimeException exception) {
                            log.error("Não foi possível remover o anexo após rollback: {}", key, exception);
                        }
                    }
                }
            }
        });
        for (int position = 0; position < files.size(); position++) {
            var file = files.get(position);
            var uploaded = bucket.upload(new UploadFileRequest(file, "posts/" + post.getPostId()));
            uploadedKeys.add(uploaded.key());
            media.save(PostMediaEntity.builder()
                    .post(post)
                    .storageKey(uploaded.key())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .position(position)
                    .build());
        }
        media.flush();
    }

    public void removeAttachments(PostEntity post) {
        var attachments = media.findByPost_IdInOrderByPositionAsc(List.of(post.getId()));
        var keys = attachments.stream()
                .map(PostMediaEntity::getStorageKey)
                .filter(java.util.Objects::nonNull)
                .toList();
        media.deleteAllInBatch(attachments);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String key : keys) {
                    try {
                        bucket.delete(key);
                    } catch (RuntimeException exception) {
                        log.error("Falha na limpeza do anexo da publicação removida: {}", key, exception);
                    }
                }
            }
        });
    }

    public void attachTenor(PostEntity post, String tenorId) {
        media.saveAndFlush(PostMediaEntity.builder()
                .post(post)
                .tenorId(tenorId)
                .contentType("image/gif")
                .position(0)
                .size(0)
                .build());
    }

    @Transactional(readOnly = true)
    public DownloadFileResponse download(UUID postId, UUID mediaId) {
        auth.requireAuthenticated();
        var attachment = media.findByPost_PostIdAndMediaId(postId, mediaId)
                .orElseThrow(() -> new PostNotFoundException("Anexo não encontrado nesta publicação."));
        if (attachment.getTenorId() != null) {
            throw new PostNotFoundException("Este GIF deve ser visualizado pela incorporação do Tenor.");
        }
        var file = bucket.download(attachment.getStorageKey());
        return new DownloadFileResponse(file.key(), file.data(), attachment.getContentType(), file.size());
    }
}
