package com.iwms.iwms.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwms.iwms.infrastructure.persistence.jpa.entity.SupabaseUserEntity;

public interface SupabaseUserRepository extends JpaRepository<SupabaseUserEntity, UUID> {
    Optional<SupabaseUserEntity> findByEmail(String email);
}


