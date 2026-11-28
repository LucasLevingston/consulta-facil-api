package com.consultafacil.core.security;

import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleAuthorizationCheckerTest {

    RoleAuthorizationChecker checker;

    @BeforeEach
    void setUp() {
        checker = new RoleAuthorizationChecker();
    }

    private Authentication authenticatedAs(String authority) {
        return new TestingAuthenticationToken(
                "user", "creds", new SimpleGrantedAuthority(authority));
    }

    // ── isAuthenticated ───────────────────────────────────────────────────

    @Test
    void isAuthenticated_withAuthenticatedToken_returnsTrue() {
        Authentication auth = authenticatedAs(UserRole.PATIENT.getAuthority());
        assertThat(checker.isAuthenticated(auth)).isTrue();
    }

    @Test
    void isAuthenticated_withNullAuthentication_returnsFalse() {
        assertThat(checker.isAuthenticated(null)).isFalse();
    }

    @Test
    void isAuthenticated_withUnauthenticatedToken_returnsFalse() {
        Authentication auth = new TestingAuthenticationToken("user", "creds");
        auth.setAuthenticated(false);
        assertThat(checker.isAuthenticated(auth)).isFalse();
    }

    // ── is(auth, single role) ─────────────────────────────────────────────

    @Test
    void is_withMatchingRole_returnsTrue() {
        Authentication auth = authenticatedAs(UserRole.ADMIN.getAuthority());
        assertThat(checker.is(auth, UserRole.ADMIN)).isTrue();
    }

    @Test
    void is_withNonMatchingRole_returnsFalse() {
        Authentication auth = authenticatedAs(UserRole.PATIENT.getAuthority());
        assertThat(checker.is(auth, UserRole.ADMIN)).isFalse();
    }

    @Test
    void is_withNullAuthentication_returnsFalse() {
        assertThat(checker.is(null, UserRole.ADMIN)).isFalse();
    }

    @Test
    void is_withUnauthenticatedToken_returnsFalse() {
        Authentication auth = new TestingAuthenticationToken("user", "creds");
        auth.setAuthenticated(false);
        assertThat(checker.is(auth, UserRole.PATIENT)).isFalse();
    }

    // ── is(auth, multiple roles) ──────────────────────────────────────────

    @Test
    void is_withAnyMatchingRoleInVarargs_returnsTrue() {
        Authentication auth = authenticatedAs(UserRole.PROFESSIONAL.getAuthority());
        assertThat(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).isTrue();
    }

    @Test
    void is_withNoMatchingRoleInVarargs_returnsFalse() {
        Authentication auth = authenticatedAs(UserRole.PATIENT.getAuthority());
        assertThat(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).isFalse();
    }

    @Test
    void is_withEmptyRolesArray_returnsFalse() {
        Authentication auth = authenticatedAs(UserRole.ADMIN.getAuthority());
        assertThat(checker.is(auth)).isFalse();
    }

    // ── authority resolution ──────────────────────────────────────────────

    @Test
    void is_professionalRole_matchesCorrectAuthority() {
        Authentication auth = authenticatedAs(UserRole.PROFESSIONAL.getAuthority());
        assertThat(checker.is(auth, UserRole.PROFESSIONAL)).isTrue();
        assertThat(checker.is(auth, UserRole.PATIENT)).isFalse();
        assertThat(checker.is(auth, UserRole.ADMIN)).isFalse();
    }

    @Test
    void is_tokenWithNoAuthorities_returnsFalse() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "creds", List.of());
        assertThat(checker.is(auth, UserRole.ADMIN)).isFalse();
    }

    @Test
    void is_receptionistRole_matchesCorrectAuthority() {
        Authentication auth = authenticatedAs(UserRole.RECEPTIONIST.getAuthority());
        assertThat(checker.is(auth, UserRole.RECEPTIONIST)).isTrue();
        assertThat(checker.is(auth, UserRole.ADMIN)).isFalse();
    }
}
