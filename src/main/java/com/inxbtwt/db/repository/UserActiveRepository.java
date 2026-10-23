package com.inxbtwt.db.repository;

import com.inxbtwt.db.entity.UserActiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActiveRepository extends JpaRepository<UserActiveEntity, Integer> {

}
