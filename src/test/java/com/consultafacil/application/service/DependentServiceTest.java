package com.consultafacil.application.service;

import com.consultafacil.api.dto.dependent.CreateDependentDTO;
import com.consultafacil.api.dto.dependent.DependentResponseDTO;
import com.consultafacil.api.dto.dependent.UpdateDependentDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Dependent;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.RelationshipType;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.DependentRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DependentServiceTest {

    @Mock DependentRepositoryPort dependentRepository;
    @Mock UserRepositoryPort userRepository;

    @InjectMocks DependentService service;

    private User patientGuardian() {
        return User.builder().id("user-1").role(UserRole.PATIENT).build();
    }

    private Dependent dependent(User guardian) {
        return Dependent.builder()
                .id("dep-1").guardian(guardian)
                .name("Ana Silva").relationship(RelationshipType.CHILD)
                .build();
    }

    @Test
    void create_validPatient_createsDependent() {
        User guardian = patientGuardian();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(guardian));
        when(dependentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateDependentDTO dto = new CreateDependentDTO("Ana Silva", null,
                LocalDate.of(2015, 3, 10), Gender.FEMALE, RelationshipType.CHILD);

        DependentResponseDTO result = service.create("user-1", dto);

        assertThat(result.name()).isEqualTo("Ana Silva");
        assertThat(result.relationship()).isEqualTo("CHILD");
        verify(dependentRepository).save(any());
    }

    @Test
    void create_professionalGuardian_throwsForbidden() {
        User professional = User.builder().id("user-2").role(UserRole.PROFESSIONAL).build();
        when(userRepository.findById("user-2")).thenReturn(Optional.of(professional));

        CreateDependentDTO dto = new CreateDependentDTO("João", null, null, null, RelationshipType.CHILD);

        assertThatThrownBy(() -> service.create("user-2", dto))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void create_userNotFound_throwsNotFound() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        CreateDependentDTO dto = new CreateDependentDTO("X", null, null, null, RelationshipType.OTHER);

        assertThatThrownBy(() -> service.create("unknown", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listByGuardian_returnsOnlyGuardianDependents() {
        User guardian = patientGuardian();
        when(dependentRepository.findByGuardianId("user-1"))
                .thenReturn(List.of(dependent(guardian)));

        List<DependentResponseDTO> result = service.listByGuardian("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Ana Silva");
    }

    @Test
    void update_partialUpdate_updatesOnlyProvidedFields() {
        User guardian = patientGuardian();
        Dependent dep = dependent(guardian);
        when(dependentRepository.findById("dep-1")).thenReturn(Optional.of(dep));
        when(dependentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateDependentDTO dto = new UpdateDependentDTO("Ana Costa", null, null, null, null);
        DependentResponseDTO result = service.update("dep-1", "user-1", dto);

        assertThat(result.name()).isEqualTo("Ana Costa");
        assertThat(result.relationship()).isEqualTo("CHILD");
    }

    @Test
    void update_wrongGuardian_throwsForbidden() {
        User otherGuardian = User.builder().id("other").role(UserRole.PATIENT).build();
        Dependent dep = dependent(otherGuardian);
        when(dependentRepository.findById("dep-1")).thenReturn(Optional.of(dep));

        UpdateDependentDTO dto = new UpdateDependentDTO("X", null, null, null, null);

        assertThatThrownBy(() -> service.update("dep-1", "user-1", dto))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void delete_validGuardian_deletesDependent() {
        User guardian = patientGuardian();
        Dependent dep = dependent(guardian);
        when(dependentRepository.findById("dep-1")).thenReturn(Optional.of(dep));

        service.delete("dep-1", "user-1");

        verify(dependentRepository).delete(dep);
    }

    @Test
    void delete_wrongGuardian_throwsForbidden() {
        User otherGuardian = User.builder().id("other").role(UserRole.PATIENT).build();
        Dependent dep = dependent(otherGuardian);
        when(dependentRepository.findById("dep-1")).thenReturn(Optional.of(dep));

        assertThatThrownBy(() -> service.delete("dep-1", "user-1"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
