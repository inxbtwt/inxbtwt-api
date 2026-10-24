package com.inxbtwt.db.repository;

import com.inxbtwt.db.entity.UserDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDataRepository extends JpaRepository<UserDataEntity, Integer> {
    Optional<UserDataEntity> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<UserDataEntity> findByUserId(UUID userId);
    boolean existsByEmailIgnoreCaseAndUserIdNot(String email, UUID userId);
}
