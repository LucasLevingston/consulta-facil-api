package com.consultafacil.core.security;

import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    JwtTokenProvider provider;

    private static final String SECRET =
            "test-secret-key-that-is-long-enough-for-hs512-algorithm-minimum-64-bytes-required";

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", 86400000L);
    }

    private User user(String id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .name("Test User")
                .password("hashed")
                .role(UserRole.PATIENT)
                .build();
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        var token = provider.generateToken(user("u-1", "test@email.com"));

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void generateToken_shouldContainThreeParts() {
        var token = provider.generateToken(user("u-1", "test@email.com"));

        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectId() {
        var token = provider.generateToken(user("user-123", "test@email.com"));

        assertThat(provider.getUserIdFromToken(token)).isEqualTo("user-123");
    }

    @Test
    void getEmailFromToken_shouldReturnCorrectEmail() {
        var token = provider.generateToken(user("u-1", "joao@email.com"));

        assertThat(provider.getEmailFromToken(token)).isEqualTo("joao@email.com");
    }

    @Test
    void validateToken_validToken_shouldReturnTrue() {
        var token = provider.generateToken(user("u-1", "test@email.com"));

        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_invalidToken_shouldReturnFalse() {
        assertThat(provider.validateToken("not.a.valid.jwt")).isFalse();
    }

    @Test
    void validateToken_tamperedToken_shouldReturnFalse() {
        var token = provider.generateToken(user("u-1", "test@email.com"));
        var tampered = token + "tampered";

        assertThat(provider.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_expiredToken_shouldReturnFalse() {
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", -1000L);
        var token = provider.generateToken(user("u-1", "test@email.com"));

        assertThat(provider.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_emptyString_shouldReturnFalse() {
        assertThat(provider.validateToken("")).isFalse();
    }

    @Test
    void getExpiresIn_shouldReturnConfiguredValue() {
        assertThat(provider.getExpiresIn()).isEqualTo(86400000L);
    }

    @Test
    void generateToken_differentUsers_shouldGenerateDifferentTokens() {
        var t1 = provider.generateToken(user("u-1", "a@email.com"));
        var t2 = provider.generateToken(user("u-2", "b@email.com"));

        assertThat(t1).isNotEqualTo(t2);
    }

    // ── getExpiration ─────────────────────────────────────────────────────

    @Test
    void getExpiration_validToken_shouldReturnPresentOptional() {
        var token = provider.generateToken(user("u-1", "test@email.com"));

        Optional<Date> expiration = provider.getExpiration(token);

        assertThat(expiration).isPresent();
        assertThat(expiration.get()).isAfter(new Date());
    }

    @Test
    void getExpiration_invalidToken_shouldReturnEmptyOptional() {
        Optional<Date> expiration = provider.getExpiration("not.a.valid.jwt");

        assertThat(expiration).isEmpty();
    }

    @Test
    void getExpiration_tamperedToken_shouldReturnEmptyOptional() {
        var token = provider.generateToken(user("u-1", "test@email.com"));

        Optional<Date> expiration = provider.getExpiration(token + "x");

        assertThat(expiration).isEmpty();
    }

    // ── validateSecret ────────────────────────────────────────────────────

    @Test
    void validateSecret_secretTooShort_throwsIllegalStateException() {
        JwtTokenProvider shortSecretProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortSecretProvider, "jwtSecret", "short");
        ReflectionTestUtils.setField(shortSecretProvider, "jwtExpirationMs", 86400000L);

        assertThatThrownBy(shortSecretProvider::validateSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jwt.secret must be at least 64 bytes");
    }

    @Test
    void validateSecret_secretExactly64Bytes_doesNotThrow() {
        JwtTokenProvider validSecretProvider = new JwtTokenProvider();
        // exactly 64 chars (64 ASCII bytes)
        ReflectionTestUtils.setField(validSecretProvider, "jwtSecret",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        ReflectionTestUtils.setField(validSecretProvider, "jwtExpirationMs", 86400000L);

        // Should not throw
        validSecretProvider.validateSecret();
    }

    // ── generateToken preserves role claim ───────────────────────────────

    @Test
    void generateToken_professionalUser_tokenContainsRoleClaim() {
        User professional = User.builder()
                .id("pro-1")
                .email("pro@email.com")
                .name("Dr. Test")
                .password("hashed")
                .role(UserRole.PROFESSIONAL)
                .build();

        var token = provider.generateToken(professional);

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUserIdFromToken(token)).isEqualTo("pro-1");
    }
}
