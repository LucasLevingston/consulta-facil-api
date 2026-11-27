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
class DeleteUserServiceTest {

    @Mock UserRepositoryPort userRepository;

    @InjectMocks DeleteUserService service;

    @Test
    void deleteUser_found_anonymizesAndSaves() {
        User user = User.builder().id("u-1").name("João").email("joao@email.com")
                .password("hashed").role(UserRole.PATIENT).build();
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        service.execute("u-1");

        verify(userRepository).save(user);
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getName()).isEqualTo("Usuário Removido");
        assertThat(user.getEmail()).isEqualTo("deleted-u-1@removed.consultafacil.com");
        assertThat(user.getCpf()).isNull();
        assertThat(user.getPhone()).isNull();
        assertThat(user.getPassword()).isNull();
    }

    @Test
    void deleteUser_notFound_throwsResourceNotFound() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
