package com.iwms.iwms.infrastructure.security;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.util.StringUtils;

/**
 * Resolves Bearer tokens from Authorization header or a cookie (e.g., "sb-access-token").
 */
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private final String cookieName;
    private final BearerTokenResolver delegate;

    public CookieBearerTokenResolver(String cookieName, BearerTokenResolver delegate) {
        this.cookieName = cookieName;
        this.delegate = delegate;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        // Prefer Authorization header when present
        String token = delegate.resolve(request);
        if (StringUtils.hasText(token)) {
            return token;
        }
        if (request.getCookies() == null) return null;
        return Optional.ofNullable(request.getCookies())
            .stream()
            .flatMap(arr -> java.util.Arrays.stream(arr))
            .filter(c -> cookieName.equals(c.getName()))
            .map(jakarta.servlet.http.Cookie::getValue)
            .filter(StringUtils::hasText)
            .findFirst()
            .orElse(null);
    }
}


