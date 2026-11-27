package com.consultafacil.application.service.user;

import com.consultafacil.api.dto.user.UpdateNotificationPreferencesDTO;
import com.consultafacil.api.dto.user.UserResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateNotificationPreferencesServiceTest {

    @Mock UserRepositoryPort userRepository;

    UpdateNotificationPreferencesService service;

    @BeforeEach
    void setUp() {
        service = new UpdateNotificationPreferencesService(userRepository, new UserMapper());
    }

    @Test
    void newUsersReceiveWhatsAppByDefault() {
        assertThat(User.builder().build().isWhatsappNotificationsEnabled()).isTrue();
    }

    @Test
    void execute_optsTheUserOutOfWhatsApp() {
        User user = User.builder().id("u1").name("Ana").build();
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = service.execute("u1",
                UpdateNotificationPreferencesDTO.builder().whatsappNotificationsEnabled(false).build());

        assertThat(user.isWhatsappNotificationsEnabled()).isFalse();
        verify(userRepository).save(user);
        assertThat(result.isWhatsappNotificationsEnabled()).isFalse();
        assertThat(result.getId()).isEqualTo("u1");
    }

    @Test
    void execute_canOptBackIn() {
        User user = User.builder().id("u1").whatsappNotificationsEnabled(false).build();
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.execute("u1",
                UpdateNotificationPreferencesDTO.builder().whatsappNotificationsEnabled(true).build())
                .isWhatsappNotificationsEnabled()).isTrue();
    }

    @Test
    void execute_unknownUser_throwsNotFound() {
        when(userRepository.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("ghost",
                UpdateNotificationPreferencesDTO.builder().whatsappNotificationsEnabled(false).build()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void newUsersReceiveEmailNotificationsEnabledByDefault() {
        assertThat(User.builder().build().isEmailNotificationsEnabled()).isTrue();
    }

    @Test
    void updateEmail_whenEmailNotificationsEnabled_false_setsFlag() {
        User user = User.builder().id("u2").name("Maria").build();
        when(userRepository.findById("u2")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = service.execute("u2",
                UpdateNotificationPreferencesDTO.builder()
                        .whatsappNotificationsEnabled(true)
                        .emailNotificationsEnabled(false)
                        .build());

        assertThat(user.isEmailNotificationsEnabled()).isFalse();
        verify(userRepository).save(user);
        assertThat(result.isEmailNotificationsEnabled()).isFalse();
    }

    @Test
    void updateEmail_whenNull_doesNotChangeEmail() {
        User user = User.builder().id("u3").name("Carlos").emailNotificationsEnabled(false).build();
        when(userRepository.findById("u3")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // emailNotificationsEnabled not set → null → should NOT change the existing false value
        UserResponseDTO result = service.execute("u3",
                UpdateNotificationPreferencesDTO.builder()
                        .whatsappNotificationsEnabled(true)
                        .build());

        assertThat(user.isEmailNotificationsEnabled()).isFalse();
        assertThat(result.isEmailNotificationsEnabled()).isFalse();
    }
}
