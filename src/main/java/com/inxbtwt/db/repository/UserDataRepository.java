package com.inxbtwt.db.repository;

import com.inxbtwt.db.entity.UserDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDataRepository extends JpaRepository<UserDataEntity, Integer>, JpaSpecificationExecutor<UserDataEntity> {
    Optional<UserDataEntity> findByEmailIgnoreCase(String email);

    @Query("select u from UserDataEntity u where lower(u.username) = lower(:username)")
    Optional<UserDataEntity> findByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
    @Query(value = "select exists(select 1 from user_data_entity where regexp_replace(phone, '[^0-9]', '', 'g') = :phone)", nativeQuery = true)
    boolean existsByNormalizedPhone(String phone);

    @Query(value = "select exists(select 1 from user_data_entity where regexp_replace(phone, '[^0-9]', '', 'g') = :phone and user_id <> :userId)", nativeQuery = true)
    boolean existsByNormalizedPhoneAndUserIdNot(String phone, UUID userId);

    Optional<UserDataEntity> findByUserId(UUID userId);
    boolean existsByEmailIgnoreCaseAndUserIdNot(String email, UUID userId);

    @Query("select (count(u) > 0) from UserDataEntity u where lower(u.username) = lower(:username)")
    boolean existsByUsernameIgnoreCase(String username);

    @Query("select (count(u) > 0) from UserDataEntity u where lower(u.username) = lower(:username) and u.userId <> :userId")
    boolean existsByUsernameIgnoreCaseAndUserIdNot(String username, UUID userId);
}
