package com.iwms.iwms.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwms.iwms.domain.model.UserInfoEntity;

public interface UserInfoRepository extends JpaRepository<UserInfoEntity, UUID> {
    Optional<UserInfoEntity> findBySupabaseUserId(UUID supabaseUserId);
}


