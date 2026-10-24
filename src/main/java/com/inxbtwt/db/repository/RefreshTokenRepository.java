package com.inxbtwt.db.repository;
import com.inxbtwt.db.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.UUID;
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    @Modifying
    @Query("delete from RefreshTokenEntity t where t.id = :id and t.userId = :userId")
    int revoke(@Param("id") UUID id, @Param("userId") UUID userId);

    @Modifying
    @Query("delete from RefreshTokenEntity t where t.id = :id and t.userId = :userId and t.expiresAt > :now")
    int consume(@Param("id") UUID id, @Param("userId") UUID userId, @Param("now") Instant now);
}
