package com.iwms.iwms.infrastructure.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@SuppressWarnings("unchecked")
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // permissions claim can be in app_metadata or user_metadata depending on setup
        Object permsClaim = jwt.getClaim("permissions");
        if (permsClaim instanceof Collection<?> perms) {
            authorities.addAll(perms.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));
        }

        // roles claim
        Object rolesClaim = jwt.getClaim("roles");
        if (rolesClaim instanceof Collection<?> roles) {
            authorities.addAll(roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));
        }

        // standard scopes: space-delimited in "scope" or array in "scp"
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

        return authorities;
    }
}


