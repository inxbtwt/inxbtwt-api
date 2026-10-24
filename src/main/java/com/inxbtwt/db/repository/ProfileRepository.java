package com.inxbtwt.db.repository;

import com.inxbtwt.db.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {

    @Query("select p from ProfileEntity p where p.user_id.userId = :userId")
    Optional<ProfileEntity> findByUserId(@Param("userId") UUID userId);
}
