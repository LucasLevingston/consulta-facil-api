package com.consultafacil.core.security;

import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock UserRepositoryPort userRepository;
    @InjectMocks CustomUserDetailsService service;

    private User user(String id, String email) {
        return User.builder().id(id).email(email).name("Test").password("hashed").role(UserRole.PATIENT).build();
    }

    @Test
    void loadUserByUsername_existingEmail_returnsDetails() {
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user("u-1", "test@email.com")));
        var details = service.loadUserByUsername("test@email.com");
        assertThat(details.getUserId()).isEqualTo("u-1");
        assertThat(details.getUsername()).isEqualTo("test@email.com");
    }

    @Test
    void loadUserByUsername_unknownEmail_throwsUsernameNotFound() {
        when(userRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("unknown@email.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown@email.com");
    }

    @Test
    void loadUserById_existingId_returnsDetails() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user("u-1", "test@email.com")));
        var details = service.loadUserById("u-1");
        assertThat(details.getUserId()).isEqualTo("u-1");
    }

    @Test
    void loadUserById_unknownId_throwsUsernameNotFound() {
        when(userRepository.findById("bad-id")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserById("bad-id"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
