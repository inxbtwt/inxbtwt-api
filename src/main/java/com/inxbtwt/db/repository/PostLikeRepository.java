package com.inxbtwt.db.repository;

import com.inxbtwt.db.entity.PostLikeEntity;
import com.inxbtwt.db.repository.projection.PostInteractionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLikeEntity, Long> {

    @Modifying
    @Query(value = "insert into post_like_entity (post_id, user_id, created_at) values (:postId, :userId, CURRENT_TIMESTAMP) on conflict (post_id, user_id) do nothing", nativeQuery = true)
    int add(@Param("postId") Long postId, @Param("userId") Integer userId);

    @Modifying
    @Query(value = "delete from post_like_entity where post_id = :postId and user_id = :userId", nativeQuery = true)
    int remove(@Param("postId") Long postId, @Param("userId") Integer userId);

    @Query("select r.post.id as postId, count(r) as total from PostLikeEntity r where r.post.id in :postIds group by r.post.id")
    List<PostInteractionCount> countForPosts(@Param("postIds") List<Long> postIds);

    @Query("select r.post.id from PostLikeEntity r where r.user.userId = :userId and r.post.id in :postIds")
    List<Long> findViewerPostIds(@Param("userId") UUID userId, @Param("postIds") List<Long> postIds);
}
