package com.consultafacil.core.security;

import com.consultafacil.domain.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class RoleAuthorizationChecker {

    public boolean is(Authentication auth, UserRole... roles) {
        if (auth == null || !auth.isAuthenticated()) return false;
        String authority = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("");
        for (UserRole role : roles) {
            if (role.getAuthority().equals(authority)) return true;
        }
        return false;
    }

    public boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }
}
