package com.consultafacil.application.service.user;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserByEmailServiceTest {

    @Mock UserRepositoryPort userRepository;

    GetUserByEmailService service;
    User user;

    @BeforeEach
    void setUp() {
        service = new GetUserByEmailService(userRepository, new UserMapper());
        user = User.builder().id("u-1").name("João").email("joao@email.com")
                .password("hashed").role(UserRole.PATIENT).build();
    }

    @Test
    void getUserByEmail_found_returnsDTO() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        var result = service.execute("joao@email.com");
        assertThat(result.getEmail()).isEqualTo("joao@email.com");
        assertThat(result.getId()).isEqualTo("u-1");
    }

    @Test
    void getUserByEmail_notFound_throwsResourceNotFound() {
        when(userRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("unknown@email.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("unknown@email.com");
    }
}
