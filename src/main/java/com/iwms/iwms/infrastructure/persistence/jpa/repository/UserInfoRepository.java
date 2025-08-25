package com.iwms.iwms.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwms.iwms.infrastructure.persistence.jpa.entity.UserInfoEntity;

public interface UserInfoRepository extends JpaRepository<UserInfoEntity, UUID> {
    Optional<UserInfoEntity> findBySupabaseUserId(UUID supabaseUserId);
    boolean existsBySuperAdminTrue();
}


