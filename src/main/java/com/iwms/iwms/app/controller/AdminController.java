package com.iwms.iwms.app.controller;

import java.util.List;
import java.util.UUID;

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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.iwms.iwms.domain.model.UserInfoEntity;
import com.iwms.iwms.domain.repository.UserInfoRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @PersistenceContext
    private EntityManager em;

    private final UserInfoRepository userInfoRepository;

    public AdminController(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    record PendingUserDto(UUID id, String email) {}

    private boolean isSuperAdmin(UUID userId) {
        return userInfoRepository.findBySupabaseUserId(userId)
            .map(UserInfoEntity::isSuperAdmin)
            .orElse(false);
    }

    @GetMapping("/pending-users")
    public ResponseEntity<?> pendingUsers(@AuthenticationPrincipal Jwt jwt) {
        UUID me = UUID.fromString(jwt.getSubject());
        if (!isSuperAdmin(me)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
            "select id, email from auth.users where coalesce((raw_app_meta_data->>'approved')::boolean, false) = false order by created_at desc")
            .getResultList();

        List<PendingUserDto> list = rows.stream()
            .map(r -> new PendingUserDto((UUID) r[0], (String) r[1]))
            .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/pending-users/{id}/approve")
    @Transactional
    public ResponseEntity<?> approve(@PathVariable("id") UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID me = UUID.fromString(jwt.getSubject());
        if (!isSuperAdmin(me)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        int updated = em.createNativeQuery(
            "update auth.users set raw_app_meta_data = raw_app_meta_data || jsonb_build_object('approved', true) where id = :id")
            .setParameter("id", id)
            .executeUpdate();
        return updated == 1 ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}


