package com.iwms.iwms.app.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iwms.iwms.infrastructure.persistence.jpa.entity.UserInfoEntity;
import com.iwms.iwms.infrastructure.persistence.jpa.repository.UserInfoRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserInfoRepository userInfoRepository;

    public AuthController(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    record StatusResponse(boolean approved, boolean superAdmin) {}

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> status(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        Optional<UserInfoEntity> userInfo = userInfoRepository.findBySupabaseUserId(userId);
        if (userInfo.isEmpty()) {
            return ResponseEntity.ok(new StatusResponse(false, false));
        }

        UserInfoEntity user = userInfo.get();
        return ResponseEntity.ok(new StatusResponse(user.isApproved(), user.isSuperAdmin()));
    }

}


