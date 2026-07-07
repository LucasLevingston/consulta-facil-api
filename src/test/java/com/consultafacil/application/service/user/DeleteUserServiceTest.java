package com.consultafacil.application.service.user;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUserServiceTest {

    @Mock UserRepositoryPort userRepository;

    @InjectMocks DeleteUserService service;

    @Test
    void deleteUser_found_callsRepositoryDelete() {
        User user = User.builder().id("u-1").name("João").email("joao@email.com")
                .password("hashed").role(UserRole.PATIENT).build();
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        service.execute("u-1");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_notFound_throwsResourceNotFound() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
