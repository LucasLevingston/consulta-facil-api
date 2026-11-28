package com.consultafacil.application.service.user;

import com.consultafacil.api.dto.user.UserResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetUserByIdServiceTest {

    @Mock UserRepositoryPort userRepository;

    GetUserByIdService service;

    @BeforeEach
    void setUp() {
        service = new GetUserByIdService(userRepository, new UserMapper());
    }

    private User buildUser(String id) {
        return User.builder()
                .id(id)
                .name("Fernanda Oliveira")
                .email("fernanda@email.com")
                .password("hashed")
                .role(UserRole.PATIENT)
                .build();
    }

    @Test
    void execute_existingId_returnsMappedDTO() {
        User user = buildUser("u-10");
        when(userRepository.findById("u-10")).thenReturn(Optional.of(user));

        UserResponseDTO result = service.execute("u-10");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("u-10");
        assertThat(result.getName()).isEqualTo("Fernanda Oliveira");
        assertThat(result.getEmail()).isEqualTo("fernanda@email.com");
    }

    @Test
    void execute_existingId_mapsRoleCorrectly() {
        User user = buildUser("u-10");
        when(userRepository.findById("u-10")).thenReturn(Optional.of(user));

        UserResponseDTO result = service.execute("u-10");

        assertThat(result.getRole()).isEqualTo(UserRole.PATIENT);
    }

    @Test
    void execute_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
