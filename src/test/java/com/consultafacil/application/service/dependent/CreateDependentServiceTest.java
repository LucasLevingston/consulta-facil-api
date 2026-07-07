package com.consultafacil.application.service.dependent;

import com.consultafacil.api.dto.dependent.CreateDependentDTO;
import com.consultafacil.api.dto.dependent.DependentResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.RelationshipType;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.DependentRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateDependentServiceTest {

    @Mock DependentRepositoryPort dependentRepository;
    @Mock UserRepositoryPort userRepository;

    CreateDependentService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new CreateDependentService(dependentRepository, userRepository, new DependentMapper());
    }

    private User patientGuardian() {
        return User.builder().id("user-1").role(UserRole.PATIENT).build();
    }

    @Test
    void create_validPatient_createsDependent() {
        User guardian = patientGuardian();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(guardian));
        when(dependentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateDependentDTO dto = new CreateDependentDTO("Ana Silva", null,
                LocalDate.of(2015, 3, 10), Gender.FEMALE, RelationshipType.CHILD);

        DependentResponseDTO result = service.execute("user-1", dto);

        assertThat(result.name()).isEqualTo("Ana Silva");
        assertThat(result.relationship()).isEqualTo("CHILD");
        verify(dependentRepository).save(any());
    }

    @Test
    void create_professionalGuardian_throwsForbidden() {
        User professional = User.builder().id("user-2").role(UserRole.PROFESSIONAL).build();
        when(userRepository.findById("user-2")).thenReturn(Optional.of(professional));

        CreateDependentDTO dto = new CreateDependentDTO("João", null, null, null, RelationshipType.CHILD);

        assertThatThrownBy(() -> service.execute("user-2", dto))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void create_userNotFound_throwsNotFound() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        CreateDependentDTO dto = new CreateDependentDTO("X", null, null, null, RelationshipType.OTHER);

        assertThatThrownBy(() -> service.execute("unknown", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
