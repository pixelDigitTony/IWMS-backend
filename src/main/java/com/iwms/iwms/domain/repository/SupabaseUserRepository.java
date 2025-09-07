package com.iwms.iwms.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwms.iwms.domain.model.SupabaseUserEntity;

public interface SupabaseUserRepository extends JpaRepository<SupabaseUserEntity, UUID> {
    Optional<SupabaseUserEntity> findByEmail(String email);
}


