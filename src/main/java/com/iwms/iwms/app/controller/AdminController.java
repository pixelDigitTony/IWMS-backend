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

import com.iwms.iwms.domain.model.UserInfoEntity;
import com.iwms.iwms.domain.model.SupabaseUserEntity;
import com.iwms.iwms.domain.repository.UserInfoRepository;
import com.iwms.iwms.domain.repository.SupabaseUserRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserInfoRepository userInfoRepository;
    private final SupabaseUserRepository supabaseUserRepository;

    public AdminController(UserInfoRepository userInfoRepository, SupabaseUserRepository supabaseUserRepository) {
        this.userInfoRepository = userInfoRepository;
        this.supabaseUserRepository = supabaseUserRepository;
    }

    record PendingUserDto(UUID id, String email) {}

    private boolean isSuperAdmin(UUID userId) {
        var userInfo = userInfoRepository.findBySupabaseUserId(userId);
        if (userInfo.isEmpty()) {
            return false;
        }
        
        var user = userInfo.get();
        boolean isSuperAdmin = user.isSuperAdmin();
        return isSuperAdmin;
    }

    @GetMapping("/pending-users")
    public ResponseEntity<?> pendingUsers(@AuthenticationPrincipal Jwt jwt) {
        UUID me = UUID.fromString(jwt.getSubject());
        if (!isSuperAdmin(me)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        // Get all user_info records where approved = false
        List<UserInfoEntity> pendingUsers = userInfoRepository.findAll().stream()
            .filter(user -> !user.isApproved())
            .toList();

        // Map to DTOs with email from Supabase users
        List<PendingUserDto> list = pendingUsers.stream()
            .map(user -> {
                String email = null;
                if (user.getSupabaseUserId() != null) {
                    email = supabaseUserRepository.findById(user.getSupabaseUserId())
                        .map(SupabaseUserEntity::getEmail)
                        .orElse("Unknown");
                }
                return new PendingUserDto(user.getId(), email);
            })
            .toList();
        
        return ResponseEntity.ok(list);
    }

    @PostMapping("/pending-users/{id}/approve")
    @Transactional
    public ResponseEntity<?> approve(@PathVariable("id") UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID me = UUID.fromString(jwt.getSubject());
        if (!isSuperAdmin(me)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        // Find the user_info record by ID
        var userInfo = userInfoRepository.findById(id);
        if (userInfo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Update the approval status
        var user = userInfo.get();
        user.setApproved(true);
        userInfoRepository.save(user);

        return ResponseEntity.ok().build();
    }
}


