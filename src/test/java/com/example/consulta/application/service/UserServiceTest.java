package com.example.consulta.application.service;

import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.port.out.StoragePort;
import com.example.consulta.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepositoryPort userRepository;
    @Mock StoragePort storagePort;

    @InjectMocks UserService service;

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id("u-1").name("João").email("joao@email.com")
                .password("hashed").role(UserRole.PATIENT).build();
    }

    @Test
    void getUserByEmail_found_returnsDTO() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        var result = service.getUserByEmail("joao@email.com");
        assertThat(result.getEmail()).isEqualTo("joao@email.com");
        assertThat(result.getId()).isEqualTo("u-1");
    }

    @Test
    void getUserByEmail_notFound_throwsResourceNotFound() {
        when(userRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getUserByEmail("unknown@email.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("unknown@email.com");
    }

    @Test
    void deleteUser_found_callsRepositoryDelete() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        service.deleteUser("u-1");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_notFound_throwsResourceNotFound() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteUser("bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
