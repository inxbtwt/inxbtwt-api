package com.inxbtwt.db.repository;

import com.inxbtwt.db.entity.NotificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @EntityGraph(attributePaths = {"actor", "post"})
    Slice<NotificationEntity> findByRecipient_UserIdOrderByCreatedAtDescIdDesc(UUID recipientId, Pageable pageable);

    @EntityGraph(attributePaths = {"actor", "post"})
    Optional<NotificationEntity> findByNotificationIdAndRecipient_UserId(UUID notificationId, UUID recipientId);

    @Modifying
    @Query("update NotificationEntity n set n.readAt = :readAt "
            + "where n.notificationId = :notificationId and n.recipient.userId = :recipientId and n.readAt is null")
    int markAsRead(@Param("notificationId") UUID notificationId, @Param("recipientId") UUID recipientId,
                   @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("update NotificationEntity n set n.readAt = :readAt "
            + "where n.recipient.userId = :recipientId and n.readAt is null")
    int markAllAsRead(@Param("recipientId") UUID recipientId, @Param("readAt") LocalDateTime readAt);

    long countByRecipient_UserIdAndReadAtIsNull(UUID recipientId);
}
