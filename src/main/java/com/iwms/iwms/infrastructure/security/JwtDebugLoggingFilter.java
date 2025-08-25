package com.iwms.iwms.infrastructure.security;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Debug-only filter that logs JWT header fields (alg, kid) and iss claim before validation.
 */
public class JwtDebugLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtDebugLoggingFilter.class);

    private final CookieBearerTokenResolver tokenResolver;

    private final boolean debugEnabled;

    public JwtDebugLoggingFilter(CookieBearerTokenResolver tokenResolver, boolean debugEnabled) {
        this.tokenResolver = tokenResolver;
        this.debugEnabled = debugEnabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, java.io.IOException {
        if (debugEnabled) {
            try {
                String token = tokenResolver.resolve(request);
                if (token != null && token.contains(".")) {
                    String[] parts = token.split("\\.");
                    String headerJson = new String(java.util.Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
                    String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                    // Very lightweight string scans to avoid pulling JSON libs
                    String alg = extractJsonValue(headerJson, "alg");
                    String kid = extractJsonValue(headerJson, "kid");
                    String iss = extractJsonValue(payloadJson, "iss");
                    log.info("JWT debug: alg={}, kid={}, iss={}", alg, kid, iss);
                }
            } catch (Exception ex) {
                log.warn("JWT debug logging failed: {}", ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private static String extractJsonValue(String json, String key) {
        // naive extraction: "key":"value"
        String pattern = "\"" + key + "\"" + ":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;
        int start = json.indexOf('"', idx + pattern.length());
        if (start == -1) return null;
        int end = json.indexOf('"', start + 1);
        if (end == -1) return null;
        return json.substring(start + 1, end);
    }
}


