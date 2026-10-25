package com.inxbtwt.domain.service.notification.inapp;

import com.inxbtwt.db.entity.NotificationEntity;
import com.inxbtwt.db.repository.NotificationRepository;
import com.inxbtwt.db.repository.PostRepository;
import com.inxbtwt.domain.dto.notification.NotificationCommand;
import com.inxbtwt.domain.enums.NotificationChannel;
import com.inxbtwt.domain.service.notification.NotificationSender;
import com.inxbtwt.domain.service.user.find.FindUserUseCase;
import com.inxbtwt.infra.exception.domain.post.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Persiste uma notificação no site; não envia e-mail nem transmite eventos em tempo real. */
@Service
@RequiredArgsConstructor
public class InAppNotificationSender implements NotificationSender {

    private final NotificationRepository notifications;
    private final FindUserUseCase users;
    private final PostRepository posts;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    @Transactional
    public void send(NotificationCommand command) {
        var recipient = users.findUser(command.recipientId());
        var actor = command.actorId() == null ? null : users.findUser(command.actorId());
        var post = command.postId() == null ? null : posts.findByPostId(command.postId())
                .orElseThrow(() -> new PostNotFoundException("Publicação da notificação não encontrada."));
        notifications.saveAndFlush(NotificationEntity.builder()
                .recipient(recipient)
                .actor(actor)
                .post(post)
                .type(command.type())
                .title(command.title().strip())
                .message(command.message().strip())
                .build());
    }
}
