package com.inxbtwt.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_like_entity", uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_like_entity_post_user", columnNames = {"post_id", "user_id"})
}, indexes = {@Index(name = "idx_post_like_entity_user_created", columnList = "user_id, created_at")})
@Getter
@NoArgsConstructor
public class PostLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserDataEntity user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
