package com.consultafacil.application.service.user;

import com.consultafacil.api.dto.user.UserResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetAllUsersServiceTest {

    @Mock UserRepositoryPort userRepository;

    GetAllUsersService service;

    private User buildUser(String id, String name, String email, UserRole role) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .password("hashed")
                .role(role)
                .build();
    }

    @BeforeEach
    void setUp() {
        service = new GetAllUsersService(userRepository, new UserMapper());
    }

    @Test
    void execute_noRoleFilter_returnsAllUsers() {
        User user1 = buildUser("u-1", "Ana Costa", "ana@email.com", UserRole.PATIENT);
        User user2 = buildUser("u-2", "Bruno Lima", "bruno@email.com", UserRole.PROFESSIONAL);
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user1, user2)));

        Page<UserResponseDTO> result = service.execute(0, 10, null);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void execute_withRoleFilter_returnsFilteredUsers() {
        User patient = buildUser("u-3", "Carla Santos", "carla@email.com", UserRole.PATIENT);
        when(userRepository.findByRole(eq(UserRole.PATIENT), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(patient)));

        Page<UserResponseDTO> result = service.execute(0, 10, UserRole.PATIENT);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getRole()).isEqualTo(UserRole.PATIENT);
    }

    @Test
    void execute_mapsUserFieldsCorrectly() {
        User user = buildUser("u-4", "Diego Rocha", "diego@email.com", UserRole.ADMIN);
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        Page<UserResponseDTO> result = service.execute(0, 10, null);

        UserResponseDTO dto = result.getContent().get(0);
        assertThat(dto.getId()).isEqualTo("u-4");
        assertThat(dto.getName()).isEqualTo("Diego Rocha");
        assertThat(dto.getEmail()).isEqualTo("diego@email.com");
    }

    @Test
    void execute_emptyRepository_returnsEmptyPage() {
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<UserResponseDTO> result = service.execute(0, 10, null);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}
