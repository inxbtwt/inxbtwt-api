package com.inxbtwt.db.repository;

import com.inxbtwt.db.entity.PostMediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostMediaRepository extends JpaRepository<PostMediaEntity, Long> {

    List<PostMediaEntity> findByPost_IdInOrderByPositionAsc(List<Long> postIds);

    Optional<PostMediaEntity> findByPost_PostIdAndMediaId(UUID postId, UUID mediaId);
}
