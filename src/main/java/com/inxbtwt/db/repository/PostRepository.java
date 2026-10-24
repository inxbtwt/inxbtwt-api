package com.inxbtwt.db.repository;

import com.inxbtwt.db.entity.PostEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.inxbtwt.db.repository.projection.PostInteractionCount;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    @EntityGraph(attributePaths = {"author", "parent", "parent.author"})
    Slice<PostEntity> findByParentIsNullAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "parent", "parent.author"})
    @Query("select p from PostEntity p left join PostRepostEntity r on r.post = p and r.user.userId = :authorId "
            + "where p.deletedAt is null and ((p.author.userId = :authorId and p.parent is null) or r.id is not null) "
            + "order by coalesce(r.createdAt, p.createdAt) desc, p.id desc")
    Slice<PostEntity> findPublications(@Param("authorId") UUID authorId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "parent", "parent.author"})
    Slice<PostEntity> findByAuthor_UserIdAndParentIsNotNullAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(UUID authorId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "parent", "parent.author"})
    @Query("select p from PostEntity p join PostLikeEntity l on l.post = p where p.deletedAt is null and l.user.userId = :authorId order by l.createdAt desc, l.id desc")
    Slice<PostEntity> findLikedPosts(@Param("authorId") UUID authorId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "parent", "parent.author"})
    Slice<PostEntity> findByParent_PostIdAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(UUID postId, Pageable pageable);

    @Query("select p.parent.id as postId, count(p) as total from PostEntity p where p.deletedAt is null and p.parent.id in :postIds group by p.parent.id")
    List<PostInteractionCount> countComments(@Param("postIds") List<Long> postIds);

    @EntityGraph(attributePaths = {"author", "parent", "parent.author"})
    @Query("select p from PostEntity p where p.postId = :postId and p.deletedAt is null")
    Optional<PostEntity> findByPostId(@Param("postId") UUID postId);
}
