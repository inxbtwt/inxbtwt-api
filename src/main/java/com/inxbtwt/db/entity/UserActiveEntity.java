package com.inxbtwt.db.entity;

import com.inxbtwt.domain.enums.StatusActiveUserEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "UserActiveEntity")
@Table(name = "user_active_entity")
public class UserActiveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private UserDataEntity userId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusActiveUserEnum status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

}