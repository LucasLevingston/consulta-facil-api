package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class UserTest {

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("u-1").name("João").email("j@e.com")
                .password("x").role(UserRole.PATIENT).build();
    }

    @Test void recordFailedLogin_incrementsCount() {
        user.recordFailedLogin(5, 15);
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test void recordFailedLogin_reachesMax_setsLock() {
        for (int i = 0; i < 5; i++) user.recordFailedLogin(5, 15);
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test void resetLoginAttempts_clearsCountAndLock() {
        user.recordFailedLogin(5, 15);
        user.resetLoginAttempts();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test void isCurrentlyLocked_notLocked_returnsFalse() {
        assertThat(user.isCurrentlyLocked()).isFalse();
    }

    @Test void isCurrentlyLocked_lockedFuture_returnsTrue() {
        user.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        assertThat(user.isCurrentlyLocked()).isTrue();
    }

    @Test void isCurrentlyLocked_lockExpired_returnsFalse() {
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        assertThat(user.isCurrentlyLocked()).isFalse();
    }

    @Test void promote_changesRole() {
        user.promote(UserRole.PROFESSIONAL);
        assertThat(user.getRole()).isEqualTo(UserRole.PROFESSIONAL);
    }

    @Test void clearAvatar_setsNulls() {
        user.updateAvatar("url", "id");
        user.clearAvatar();
        assertThat(user.getImageUrl()).isNull();
        assertThat(user.getImageId()).isNull();
    }

    @Test void updateAvatar_setsValues() {
        user.updateAvatar("https://img.url", "img-123");
        assertThat(user.getImageUrl()).isEqualTo("https://img.url");
        assertThat(user.getImageId()).isEqualTo("img-123");
    }
}
