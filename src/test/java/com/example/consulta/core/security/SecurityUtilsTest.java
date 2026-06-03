package com.example.consulta.core.security;

import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void setAuth(String id, String email) {
        User user = User.builder().id(id).email(email).name("Test").password("x").role(UserRole.PATIENT).build();
        var details = new CustomUserDetails(user);
        var auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getCurrentUserId_authenticated_returnsId() {
        setAuth("user-123", "test@email.com");
        assertThat(SecurityUtils.getCurrentUserId()).isEqualTo("user-123");
    }

    @Test
    void getCurrentUserEmail_authenticated_returnsEmail() {
        setAuth("user-123", "test@email.com");
        assertThat(SecurityUtils.getCurrentUserEmail()).isEqualTo("test@email.com");
    }

    @Test
    void getCurrentUserId_noAuthentication_throws() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(SecurityUtils::getCurrentUserId).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getCurrentUserEmail_noAuthentication_throws() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(SecurityUtils::getCurrentUserEmail).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void isAuthenticated_withAuth_returnsTrue() {
        setAuth("u-1", "a@b.com");
        assertThat(SecurityUtils.isAuthenticated()).isTrue();
    }

    @Test
    void isAuthenticated_noAuth_returnsFalse() {
        SecurityContextHolder.clearContext();
        assertThat(SecurityUtils.isAuthenticated()).isFalse();
    }
}
