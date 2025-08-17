package com.iwms.iwms.infrastructure.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("rbac")
public class RbacEvaluator {
    public boolean hasPermission(Authentication auth, String permission) {
        if (auth == null || !auth.isAuthenticated()) return false;
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        return authorities.stream().anyMatch(a -> permission.equals(a.getAuthority()));
    }
}


