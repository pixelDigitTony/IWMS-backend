package com.iwms.iwms.infrastructure.persistence.jpa.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Immutable
@Table(schema = "auth", name = "users")
public class SupabaseUserEntity {

    @Id
    private UUID id;

    @Column
    private String email;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "last_sign_in_at")
    private OffsetDateTime lastSignInAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getLastSignInAt() { return lastSignInAt; }
    public void setLastSignInAt(OffsetDateTime lastSignInAt) { this.lastSignInAt = lastSignInAt; }
}


