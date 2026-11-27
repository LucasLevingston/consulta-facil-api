package com.consultafacil.application.service.user;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnonymizeUserServiceTest {

    @Mock UserRepositoryPort userRepository;

    @InjectMocks AnonymizeUserService service;

    private User buildUser() {
        return User.builder()
                .id("u-1")
                .name("João Silva")
                .email("joao@email.com")
                .password("hashed-password")
                .cpf("123.456.789-00")
                .phone("11999999999")
                .role(UserRole.PATIENT)
                .build();
    }

    @Test
    void anonymize_setsNameToRemovedUser() {
        User user = buildUser();
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        service.execute("u-1");

        assertThat(user.getName()).isEqualTo("Usuário Removido");
    }

    @Test
    void anonymize_setsAnonymizedEmail() {
        User user = buildUser();
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        service.execute("u-1");

        assertThat(user.getEmail()).isEqualTo("deleted-u-1@removed.consultafacil.com");
    }

    @Test
    void anonymize_nullifiesSensitiveFields() {
        User user = buildUser();
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        service.execute("u-1");

        assertThat(user.getCpf()).isNull();
        assertThat(user.getPhone()).isNull();
        assertThat(user.getPassword()).isNull();
    }

    @Test
    void anonymize_setsDeletedAt() {
        User user = buildUser();
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        service.execute("u-1");

        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    void anonymize_callsRepositorySave() {
        User user = buildUser();
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        service.execute("u-1");

        verify(userRepository).save(user);
    }

    @Test
    void anonymize_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
