package com.iwms.iwms.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwms.iwms.infrastructure.persistence.jpa.entity.AppUserEntity;

public interface AppUserRepository extends JpaRepository<AppUserEntity, UUID> {
    Optional<AppUserEntity> findBySupabaseUserId(UUID supabaseUserId);
    boolean existsBySuperAdminTrue();
}


