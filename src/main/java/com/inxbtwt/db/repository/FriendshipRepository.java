package com.inxbtwt.db.repository;

import com.inxbtwt.db.entity.FriendshipEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<FriendshipEntity, Long> {

    @Modifying
    @Query(value = "insert into friendship_entity (friendship_id, first_user_id, second_user_id, status, created_at, updated_at) "
            + "values (:friendshipId, :firstId, :secondId, 'NONE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) "
            + "on conflict (first_user_id, second_user_id) do nothing", nativeQuery = true)
    int ensurePair(@Param("friendshipId") UUID friendshipId, @Param("firstId") Integer firstId,
                   @Param("secondId") Integer secondId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FriendshipEntity f where f.firstUser.id = :firstId and f.secondUser.id = :secondId")
    Optional<FriendshipEntity> lockPair(@Param("firstId") Integer firstId, @Param("secondId") Integer secondId);

    @EntityGraph(attributePaths = {"firstUser", "secondUser", "requester"})
    Optional<FriendshipEntity> findByFirstUser_IdAndSecondUser_Id(Integer firstId, Integer secondId);

    @EntityGraph(attributePaths = {"firstUser", "secondUser", "requester"})
    @Query("select f from FriendshipEntity f where f.status = com.inxbtwt.domain.enums.FriendshipStatus.ACCEPTED "
            + "and (f.firstUser.userId = :userId or f.secondUser.userId = :userId) "
            + "order by f.acceptedAt desc, f.id desc")
    Slice<FriendshipEntity> findFriends(@Param("userId") UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"firstUser", "secondUser", "requester"})
    @Query("select f from FriendshipEntity f where f.status = com.inxbtwt.domain.enums.FriendshipStatus.PENDING "
            + "and f.requester.userId = :userId order by f.requestedAt desc, f.id desc")
    Slice<FriendshipEntity> findSentRequests(@Param("userId") UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"firstUser", "secondUser", "requester"})
    @Query("select f from FriendshipEntity f where f.status = com.inxbtwt.domain.enums.FriendshipStatus.PENDING "
            + "and f.requester.userId <> :userId "
            + "and (f.firstUser.userId = :userId or f.secondUser.userId = :userId) "
            + "order by f.requestedAt desc, f.id desc")
    Slice<FriendshipEntity> findReceivedRequests(@Param("userId") UUID userId, Pageable pageable);

    @Query("select count(f) from FriendshipEntity f where f.status = com.inxbtwt.domain.enums.FriendshipStatus.ACCEPTED "
            + "and (f.firstUser.userId = :userId or f.secondUser.userId = :userId)")
    long countFriends(@Param("userId") UUID userId);
}
