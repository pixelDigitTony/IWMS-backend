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

import com.iwms.iwms.infrastructure.persistence.jpa.entity.AppUserEntity;
import com.iwms.iwms.infrastructure.persistence.jpa.repository.AppUserRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AppUserRepository users;

    public AuthController(AppUserRepository users) {
        this.users = users;
    }

    record StatusResponse(boolean approved, boolean superAdmin) {}

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> status(@AuthenticationPrincipal Jwt jwt) {
        UUID supabaseUserId = UUID.fromString(jwt.getSubject());
        return users.findBySupabaseUserId(supabaseUserId)
                .map(u -> ResponseEntity.ok(new StatusResponse(u.isApproved(), u.isSuperAdmin())))
                .orElseGet(() -> {
                    // Auto-register authenticated Supabase user into app_user with approved=false
                    AppUserEntity e = new AppUserEntity();
                    e.setId(java.util.UUID.randomUUID());
                    e.setSupabaseUserId(supabaseUserId);
                    e.setEmail(jwt.getClaim("email"));
                    e.setApproved(false);
                    e.setSuperAdmin(false);
                    e.setCreatedAt(java.time.OffsetDateTime.now());
                    users.save(e);
                    return ResponseEntity.ok(new StatusResponse(false, false));
                });
    }

    @PostMapping("/bootstrap")
    @Transactional
    public ResponseEntity<Void> bootstrap(@AuthenticationPrincipal Jwt jwt) {
        UUID supabaseUserId = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaim("email");

        AppUserEntity user = users.findBySupabaseUserId(supabaseUserId).orElseGet(() -> {
            AppUserEntity e = new AppUserEntity();
            e.setId(java.util.UUID.randomUUID());
            e.setSupabaseUserId(supabaseUserId);
            e.setEmail(email);
            e.setApproved(true); // initial approved; may be toggled by admins later
            e.setSuperAdmin(false);
            e.setCreatedAt(java.time.OffsetDateTime.now());
            return users.save(e);
        });

        if (!users.existsBySuperAdminTrue()) {
            user.setSuperAdmin(true);
            user.setApproved(true);
            users.save(user);
        }
        return ResponseEntity.ok().build();
    }
}


