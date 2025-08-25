package com.iwms.iwms.app.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PersistenceContext
    private EntityManager em;

    record StatusResponse(boolean approved, boolean superAdmin) {}

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> status(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        // Read flags from auth.users.raw_app_meta_data
        Object[] row = (Object[]) em.createNativeQuery(
            "select coalesce((raw_app_meta_data->>'approved')::boolean, false), " +
            "coalesce((raw_app_meta_data->>'is_super_admin')::boolean, false) " +
            "from auth.users where id = :id")
            .setParameter("id", userId)
            .getSingleResult();

        boolean approved = row != null && row[0] != null ? (Boolean) row[0] : false;
        boolean superAdmin = row != null && row[1] != null ? (Boolean) row[1] : false;
        return ResponseEntity.ok(new StatusResponse(approved, superAdmin));
    }

    
}


