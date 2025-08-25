package com.iwms.iwms.app.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import com.iwms.iwms.infrastructure.persistence.jpa.entity.UserInfoEntity;
import com.iwms.iwms.infrastructure.persistence.jpa.repository.UserInfoRepository;
import com.iwms.iwms.infrastructure.persistence.jpa.entity.SupabaseUserEntity;
import com.iwms.iwms.infrastructure.persistence.jpa.repository.SupabaseUserRepository;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UserInfoRepository users;
    private final SupabaseUserRepository supabaseUsers;

    public UsersController(UserInfoRepository users, SupabaseUserRepository supabaseUsers) {
        this.users = users;
        this.supabaseUsers = supabaseUsers;
    }

    record UserDto(UUID id, String email, boolean approved, boolean superAdmin) {}
    record CreateUserRequest(String email) {}

    @GetMapping
    @PreAuthorize("hasAuthority('users.manage') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserDto>> list() {
        List<UserDto> list = users.findAll().stream()
            .map(u -> {
                String email = null;
                UUID supabaseId = u.getSupabaseUserId();
                if (supabaseId != null) {
                    Optional<SupabaseUserEntity> su = supabaseUsers.findById(supabaseId);
                    email = su.map(SupabaseUserEntity::getEmail).orElse(null);
                }
                return new UserDto(u.getId(), email, u.isApproved(), u.isSuperAdmin());
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<Void> register(@RequestBody CreateUserRequest req) {
        if (req == null || req.email() == null || req.email().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<SupabaseUserEntity> supabaseUser = supabaseUsers.findByEmail(req.email());
        if (supabaseUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UUID supabaseId = supabaseUser.get().getId();
        Optional<UserInfoEntity> existing = users.findBySupabaseUserId(supabaseId);
        if (existing.isEmpty()) {
            UserInfoEntity created = new UserInfoEntity();
            created.setId(UUID.randomUUID());
            created.setSupabaseUserId(supabaseId);
            created.setApproved(false);
            created.setSuperAdmin(false);
            created.setCreatedAt(java.time.OffsetDateTime.now());
            users.save(created);
        }
        return ResponseEntity.ok().build();
    }
}


