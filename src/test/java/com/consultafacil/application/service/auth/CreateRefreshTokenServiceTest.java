package com.consultafacil.application.service.auth;

import com.consultafacil.domain.entity.RefreshToken;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.auth.RefreshTokenRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateRefreshTokenServiceTest {

    @Mock RefreshTokenRepositoryPort refreshTokenRepository;

    @InjectMocks CreateRefreshTokenService service;

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id("u-1").email("u@e.com").name("User")
                .password("x").role(UserRole.PATIENT).build();

        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createFor_savesTokenWithCorrectUser() {
        var result = service.createFor(user);

        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.isRevoked()).isFalse();
    }
}
