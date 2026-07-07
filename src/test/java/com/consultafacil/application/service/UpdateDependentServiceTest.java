package com.consultafacil.application.service;

import com.consultafacil.api.dto.dependent.DependentResponseDTO;
import com.consultafacil.api.dto.dependent.UpdateDependentDTO;
import com.consultafacil.domain.entity.Dependent;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.RelationshipType;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.DependentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateDependentServiceTest {

    @Mock DependentRepositoryPort dependentRepository;

    UpdateDependentService service;

    @BeforeEach
    void setUp() {
        service = new UpdateDependentService(dependentRepository,
                new DependentAccessValidator(dependentRepository), new DependentMapper());
    }

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
    void update_partialUpdate_updatesOnlyProvidedFields() {
        User guardian = patientGuardian();
        Dependent dep = dependent(guardian);
        when(dependentRepository.findById("dep-1")).thenReturn(Optional.of(dep));
        when(dependentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateDependentDTO dto = new UpdateDependentDTO("Ana Costa", null, null, null, null);
        DependentResponseDTO result = service.execute("dep-1", "user-1", dto);

        assertThat(result.name()).isEqualTo("Ana Costa");
        assertThat(result.relationship()).isEqualTo("CHILD");
    }

    @Test
    void update_wrongGuardian_throwsForbidden() {
        User otherGuardian = User.builder().id("other").role(UserRole.PATIENT).build();
        Dependent dep = dependent(otherGuardian);
        when(dependentRepository.findById("dep-1")).thenReturn(Optional.of(dep));

        UpdateDependentDTO dto = new UpdateDependentDTO("X", null, null, null, null);

        assertThatThrownBy(() -> service.execute("dep-1", "user-1", dto))
                .isInstanceOf(ResponseStatusException.class);
    }
}
