package com.iwms.iwms.app.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iwms.iwms.infrastructure.persistence.jpa.entity.AppUserEntity;
import com.iwms.iwms.infrastructure.persistence.jpa.repository.AppUserRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AppUserRepository users;

    public AdminController(AppUserRepository users) {
        this.users = users;
    }

    record PendingUserDto(UUID id, String email) {}

    private boolean requireSuperAdmin(Jwt jwt) {
        return users.findBySupabaseUserId(UUID.fromString(jwt.getSubject()))
            .map(AppUserEntity::isSuperAdmin)
            .orElse(false);
    }

    @GetMapping("/pending-users")
    public ResponseEntity<?> pendingUsers(@AuthenticationPrincipal Jwt jwt) {
        if (!requireSuperAdmin(jwt)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<PendingUserDto> list = users.findAll().stream()
            .filter(u -> !u.isApproved())
            .map(u -> new PendingUserDto(u.getId(), u.getEmail()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/pending-users/{id}/approve")
    @Transactional
    public ResponseEntity<?> approve(@PathVariable("id") UUID id, @AuthenticationPrincipal Jwt jwt) {
        if (!requireSuperAdmin(jwt)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return users.findById(id)
            .map(u -> {
                u.setApproved(true);
                users.save(u);
                return ResponseEntity.ok().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
}


