package com.consultafacil.application.service.auth;

import com.consultafacil.core.security.JwtTokenProvider;
import com.consultafacil.domain.entity.RefreshToken;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.auth.GoogleOAuthPort;
import com.consultafacil.domain.port.out.auth.GoogleOAuthPort.GoogleUserInfo;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoogleLoginServiceTest {

    @Mock GoogleOAuthPort googleOAuthPort;
    @Mock UserRepositoryPort userRepository;
    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock CreateRefreshTokenService createRefreshTokenService;

    @InjectMocks GoogleLoginService service;

    GoogleUserInfo info;
    User existingUser;

    @BeforeEach
    void setUp() {
        info = new GoogleUserInfo("sub-123", "test@gmail.com", "Test User", "http://pic.jpg");
        existingUser = User.builder().id("u-1").name("Test User").email("test@gmail.com")
                .googleId("sub-123").role(UserRole.PATIENT).build();

        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt");
        when(jwtTokenProvider.getExpiresIn()).thenReturn(86400L);
        when(createRefreshTokenService.createFor(any())).thenReturn(
                RefreshToken.builder().token("refresh").build());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_existingGoogleUser_returnsToken() {
        when(googleOAuthPort.verifyIdToken("id-token")).thenReturn(info);
        when(userRepository.findByGoogleId("sub-123")).thenReturn(Optional.of(existingUser));

        var result = service.execute("id-token");

        assertThat(result.getToken()).isEqualTo("jwt");
        assertThat(result.getUserId()).isEqualTo("u-1");
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void execute_userFoundByEmail_linksGoogleId() {
        User emailUser = User.builder().id("u-2").name("Test").email("test@gmail.com")
                .googleId(null).role(UserRole.PATIENT).build();

        when(googleOAuthPort.verifyIdToken("id-token")).thenReturn(info);
        when(userRepository.findByGoogleId("sub-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(emailUser));

        service.execute("id-token");

        assertThat(emailUser.getGoogleId()).isEqualTo("sub-123");
        verify(userRepository).save(emailUser);
    }

    @Test
    void execute_newUser_createsProfileAndPatient() {
        when(googleOAuthPort.verifyIdToken("id-token")).thenReturn(info);
        when(userRepository.findByGoogleId("sub-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        service.execute("id-token");

        verify(userRepository, atLeast(1)).save(any());
        verify(patientProfileRepository).save(any());
    }

    @Test
    void execute_returnsRefreshToken() {
        when(googleOAuthPort.verifyIdToken("id-token")).thenReturn(info);
        when(userRepository.findByGoogleId("sub-123")).thenReturn(Optional.of(existingUser));

        var result = service.execute("id-token");

        assertThat(result.getRefreshToken()).isEqualTo("refresh");
    }
}
