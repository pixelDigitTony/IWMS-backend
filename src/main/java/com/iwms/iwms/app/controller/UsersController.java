package com.iwms.iwms.app.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iwms.iwms.infrastructure.persistence.jpa.entity.AppUserEntity;
import com.iwms.iwms.infrastructure.persistence.jpa.repository.AppUserRepository;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final AppUserRepository users;

    public UsersController(AppUserRepository users) {
        this.users = users;
    }

    record UserDto(UUID id, String email, boolean approved, boolean superAdmin) {}
    record CreateUserRequest(String email) {}

    @GetMapping
    @PreAuthorize("hasAuthority('users.manage') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserDto>> list() {
        List<UserDto> list = users.findAll().stream()
            .map(u -> new UserDto(u.getId(), u.getEmail(), u.isApproved(), u.isSuperAdmin()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('users.manage') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> create(@RequestBody CreateUserRequest req) {
        AppUserEntity u = new AppUserEntity();
        u.setId(UUID.randomUUID());
        u.setEmail(req.email());
        u.setApproved(true); // as per spec: accounts created by admins are auto-approved
        u.setSuperAdmin(false);
        u.setCreatedAt(java.time.OffsetDateTime.now());
        users.save(u);
        return ResponseEntity.ok(new UserDto(u.getId(), u.getEmail(), u.isApproved(), u.isSuperAdmin()));
    }
}


