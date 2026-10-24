package com.inxbtwt.db.entity;

import com.inxbtwt.domain.enums.FriendshipStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Uma linha por par ordenado de usuários, independentemente de quem iniciou a solicitação. */
@Entity
@Table(name = "friendship_entity", uniqueConstraints = {
        @UniqueConstraint(name = "uk_friendship_pair", columnNames = {"first_user_id", "second_user_id"})
}, indexes = {
        @Index(name = "idx_friendship_first_status", columnList = "first_user_id, status"),
        @Index(name = "idx_friendship_second_status", columnList = "second_user_id, status"),
        @Index(name = "idx_friendship_requester_status", columnList = "requester_id, status")
})
@Check(constraints = "first_user_id < second_user_id")
@Getter
@NoArgsConstructor
public class FriendshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "friendship_id", nullable = false, unique = true, updatable = false)
    private UUID friendshipId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "first_user_id", nullable = false, updatable = false)
    private UserDataEntity firstUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "second_user_id", nullable = false, updatable = false)
    private UserDataEntity secondUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private UserDataEntity requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendshipStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    public void request(UserDataEntity requester) {
        this.requester = requester;
        status = FriendshipStatus.PENDING;
        requestedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        acceptedAt = null;
        updatedAt = requestedAt;
    }

    public void accept() {
        status = FriendshipStatus.ACCEPTED;
        acceptedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        updatedAt = acceptedAt;
    }

    public void clear() {
        status = FriendshipStatus.NONE;
        requester = null;
        requestedAt = null;
        acceptedAt = null;
        updatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }
}
