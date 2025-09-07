package com.iwms.iwms.app.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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

import com.iwms.iwms.domain.model.UserInfoEntity;
import com.iwms.iwms.domain.repository.UserInfoRepository;
import com.iwms.iwms.domain.model.SupabaseUserEntity;
import com.iwms.iwms.domain.repository.SupabaseUserRepository;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UserInfoRepository users;
    private final SupabaseUserRepository supabaseUsers;

    public UsersController(UserInfoRepository users, SupabaseUserRepository supabaseUsers) {
        this.users = users;
        this.supabaseUsers = supabaseUsers;
    }

    record UserDto(UUID id, String email, String displayName, boolean approved, Set<String> roles) {}
    record CreateUserRequest(String email, String displayName, String companyName) {}

    @GetMapping
    @PreAuthorize("hasAuthority('users.manage') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserDto>> list() {
        List<UserDto> list = users.findAll().stream()
            .<UserDto>map(u -> {
                String email = null;
                String displayName = null;
                UUID supabaseId = u.getSupabaseUserId();
                if (supabaseId != null) {
                    Optional<SupabaseUserEntity> su = supabaseUsers.findById(supabaseId);
                    email = su.map(SupabaseUserEntity::getEmail).orElse(null);
                    displayName = su.map(SupabaseUserEntity::getDisplayName).orElse(null);
                }
                Set<String> roleNames = u.getRoles() != null ?
                    u.getRoles().stream().map(Enum::name).collect(Collectors.toSet()) :
                    Set.of();
                return new UserDto(u.getId(), email, displayName, u.isApproved(), roleNames);
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
            created.setCompanyName(req.companyName());
            created.setCreatedAt(java.time.OffsetDateTime.now());
            users.save(created);
        }
        return ResponseEntity.ok().build();
    }
}


