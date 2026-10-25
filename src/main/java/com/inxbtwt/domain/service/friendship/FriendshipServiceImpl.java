package com.inxbtwt.domain.service.friendship;

import com.inxbtwt.db.entity.FriendshipEntity;
import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.db.repository.FriendshipRepository;
import com.inxbtwt.domain.dto.friendship.FriendUserResponse;
import com.inxbtwt.domain.dto.friendship.FriendshipPageResponse;
import com.inxbtwt.domain.dto.friendship.FriendshipResponse;
import com.inxbtwt.domain.dto.notification.NotificationCommand;
import com.inxbtwt.domain.enums.FriendRequestDirection;
import com.inxbtwt.domain.enums.FriendshipState;
import com.inxbtwt.domain.enums.FriendshipStatus;
import com.inxbtwt.domain.enums.NotificationType;
import com.inxbtwt.domain.service.notification.NotificationService;
import com.inxbtwt.domain.service.user.find.FindUserUseCase;
import com.inxbtwt.domain.util.UserAuthUtil;
import com.inxbtwt.infra.exception.domain.DomainException;
import com.inxbtwt.infra.exception.http.ConflictHttpException;
import com.inxbtwt.infra.exception.http.ForbiddenHttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Uma amizade é bilateral e nasce do aceite de uma solicitação.
 * O par é ordenado pelos IDs internos e possui restrição única no banco.
 * As mudanças bloqueiam essa mesma linha, serializando pedidos cruzados e aceites concorrentes.
 * Solicitações repetidas são idempotentes e não geram notificações duplicadas.
 * Recusar, cancelar ou desfazer retorna a relação para NONE, permitindo um novo pedido.
 * Esta camada não participa da seleção ou ordenação do feed.
 */
@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private enum ClearAction {
        DECLINE,
        CANCEL,
        REMOVE
    }

    private final FriendshipRepository friendships;
    private final FindUserUseCase users;
    private final UserAuthUtil auth;
    private final NotificationService notifications;

    @Override
    @Transactional(readOnly = true)
    public FriendshipResponse getState(UUID userId) {
        var viewer = users.findUser(auth.getUserId());
        var other = users.findUser(userId);
        if (viewer.getUserId().equals(userId)) {
            return response(other, null, viewer.getUserId());
        }
        var pair = friendships.findByFirstUser_IdAndSecondUser_Id(
                Math.min(viewer.getId(), other.getId()), Math.max(viewer.getId(), other.getId()));
        return response(other, pair.orElse(null), viewer.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public FriendshipPageResponse getFriends(int page, int size) {
        var viewer = auth.getUserId();
        return page(friendships.findFriends(viewer, pageable(page, size)), viewer);
    }

    @Override
    @Transactional(readOnly = true)
    public FriendshipPageResponse getRequests(FriendRequestDirection direction, int page, int size) {
        var viewer = auth.getUserId();
        if (direction == null) {
            throw new DomainException("Informe a direção das solicitações.");
        }
        var pageable = pageable(page, size);
        var result = direction == FriendRequestDirection.RECEIVED
                ? friendships.findReceivedRequests(viewer, pageable)
                : friendships.findSentRequests(viewer, pageable);
        return page(result, viewer);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFriends() {
        return friendships.countFriends(auth.getUserId());
    }

    @Override
    @Transactional
    public FriendshipResponse request(UUID userId) {
        var viewer = users.findUser(auth.getUserId());
        var other = otherUser(viewer, userId);
        var pair = lockedPair(viewer, other);
        if (pair.getStatus() == FriendshipStatus.NONE) {
            pair.request(viewer);
            friendships.saveAndFlush(pair);
            notify(other, viewer, NotificationType.FRIEND_REQUEST_RECEIVED,
                    "Solicitação de amizade", "enviou uma solicitação de amizade.");
        }
        return response(other, pair, viewer.getUserId());
    }

    @Override
    @Transactional
    public FriendshipResponse accept(UUID userId) {
        var viewer = users.findUser(auth.getUserId());
        var other = otherUser(viewer, userId);
        var pair = lockedPair(viewer, other);
        if (pair.getStatus() == FriendshipStatus.ACCEPTED) {
            return response(other, pair, viewer.getUserId());
        }
        requirePending(pair);
        requireRecipient(pair, viewer);
        pair.accept();
        friendships.saveAndFlush(pair);
        notify(other, viewer, NotificationType.FRIEND_REQUEST_ACCEPTED,
                "Solicitação de amizade aceita", "aceitou sua solicitação de amizade.");
        return response(other, pair, viewer.getUserId());
    }

    @Override
    @Transactional
    public FriendshipResponse decline(UUID userId) {
        return clear(userId, ClearAction.DECLINE);
    }

    @Override
    @Transactional
    public FriendshipResponse cancel(UUID userId) {
        return clear(userId, ClearAction.CANCEL);
    }

    @Override
    @Transactional
    public FriendshipResponse remove(UUID userId) {
        return clear(userId, ClearAction.REMOVE);
    }

    private FriendshipResponse clear(UUID userId, ClearAction action) {
        var viewer = users.findUser(auth.getUserId());
        var other = otherUser(viewer, userId);
        var pair = lockedPair(viewer, other);
        if (pair.getStatus() == FriendshipStatus.NONE) {
            return response(other, pair, viewer.getUserId());
        }
        if (action == ClearAction.REMOVE) {
            if (pair.getStatus() != FriendshipStatus.ACCEPTED) {
                throw new ConflictHttpException("Esta amizade ainda não foi aceita.");
            }
        } else {
            requirePending(pair);
            if (action == ClearAction.DECLINE) {
                requireRecipient(pair, viewer);
            } else if (!pair.getRequester().getUserId().equals(viewer.getUserId())) {
                throw new ForbiddenHttpException("Somente quem enviou pode cancelar esta solicitação.");
            }
        }
        pair.clear();
        friendships.saveAndFlush(pair);
        return response(other, pair, viewer.getUserId());
    }

    private UserDataEntity otherUser(UserDataEntity viewer, UUID userId) {
        if (viewer.getUserId().equals(userId)) {
            throw new DomainException("Você não pode enviar ou alterar uma amizade consigo mesmo.");
        }
        return users.findUser(userId);
    }

    private FriendshipEntity lockedPair(UserDataEntity viewer, UserDataEntity other) {
        int first = Math.min(viewer.getId(), other.getId());
        int second = Math.max(viewer.getId(), other.getId());
        friendships.ensurePair(UUID.randomUUID(), first, second);
        return friendships.lockPair(first, second)
                .orElseThrow(() -> new IllegalStateException("Não foi possível carregar a relação de amizade."));
    }

    private void requirePending(FriendshipEntity pair) {
        if (pair.getStatus() != FriendshipStatus.PENDING) {
            throw new ConflictHttpException("Não existe uma solicitação pendente entre estes usuários.");
        }
    }

    private void requireRecipient(FriendshipEntity pair, UserDataEntity viewer) {
        if (pair.getRequester().getUserId().equals(viewer.getUserId())) {
            throw new ForbiddenHttpException("Somente o destinatário pode aceitar ou recusar a solicitação.");
        }
    }

    private void notify(UserDataEntity recipient, UserDataEntity actor, NotificationType type,
                        String title, String action) {
        notifications.send(new NotificationCommand(recipient.getUserId(), actor.getUserId(), null,
                type, title, "@" + actor.getUsername() + " " + action));
    }

    private PageRequest pageable(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new DomainException("Informe página não negativa e tamanho entre 1 e 100.");
        }
        return PageRequest.of(page, size);
    }

    private FriendshipPageResponse page(Slice<FriendshipEntity> result, UUID viewer) {
        var content = result.getContent().stream().map(pair -> {
            var other = pair.getFirstUser().getUserId().equals(viewer) ? pair.getSecondUser() : pair.getFirstUser();
            return response(other, pair, viewer);
        }).toList();
        return new FriendshipPageResponse(content, result.getNumber(), result.getSize(),
                result.hasNext(), result.hasPrevious());
    }

    private FriendshipResponse response(UserDataEntity other, FriendshipEntity pair, UUID viewer) {
        FriendshipState state;
        if (other.getUserId().equals(viewer)) {
            state = FriendshipState.SELF;
        } else if (pair == null || pair.getStatus() == FriendshipStatus.NONE) {
            state = FriendshipState.NONE;
        } else if (pair.getStatus() == FriendshipStatus.ACCEPTED) {
            state = FriendshipState.FRIENDS;
        } else {
            state = pair.getRequester().getUserId().equals(viewer)
                    ? FriendshipState.REQUEST_SENT : FriendshipState.REQUEST_RECEIVED;
        }
        return new FriendshipResponse(new FriendUserResponse(other.getUserId(), other.getName(),
                other.getUsername(), other.getPhotoUrl()), state,
                pair == null ? null : pair.getRequestedAt(), pair == null ? null : pair.getAcceptedAt());
    }
}
