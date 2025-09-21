package com.iwms.iwms.infrastructure.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.iwms.iwms.domain.model.UserInfoEntity;
import com.iwms.iwms.domain.model.SupabaseUserEntity;
import com.iwms.iwms.domain.model.auth.Role;
import com.iwms.iwms.domain.repository.UserInfoRepository;
import com.iwms.iwms.domain.repository.SupabaseUserRepository;

@SuppressWarnings("unchecked")
public class DatabaseEnrichedJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserInfoRepository userInfoRepository;
    private final SupabaseUserRepository supabaseUserRepository;

    public DatabaseEnrichedJwtAuthenticationConverter(UserInfoRepository userInfoRepository, SupabaseUserRepository supabaseUserRepository) {
        this.userInfoRepository = userInfoRepository;
        this.supabaseUserRepository = supabaseUserRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract user ID from JWT
        UUID userId = UUID.fromString(jwt.getSubject());

        // Look up user info from database to get roles and privileges
        Optional<UserInfoEntity> userInfo = userInfoRepository.findBySupabaseUserId(userId);
        if (userInfo.isPresent()) {
            UserInfoEntity user = userInfo.get();

            // Add roles as authorities (with ROLE_ prefix for Spring Security)
            Set<Role> roles = user.getRoles();
            authorities.addAll(roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList()));

            // Add privileges as authorities (permissions)
            Set<String> privileges = user.getPrivileges();
            authorities.addAll(privileges.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));
        }

        // Also extract any roles/permissions from JWT token (fallback)
        Object permsClaim = jwt.getClaim("permissions");
        if (permsClaim instanceof Collection<?> perms) {
            authorities.addAll(perms.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));
        }

        Object rolesClaim = jwt.getClaim("roles");
        if (rolesClaim instanceof Collection<?> roles) {
            authorities.addAll(roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));
        }

        // Standard scopes
        Object scopeClaim = jwt.getClaim("scope");
        if (scopeClaim instanceof String scopeStr) {
            for (String s : scopeStr.split(" ")) {
                if (!s.isBlank()) authorities.add(new SimpleGrantedAuthority("SCOPE_" + s));
            }
        }
        Object scpClaim = jwt.getClaim("scp");
        if (scpClaim instanceof Collection<?> scopes) {
            for (Object s : scopes) {
                if (s != null) authorities.add(new SimpleGrantedAuthority("SCOPE_" + s.toString()));
            }
        }

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
