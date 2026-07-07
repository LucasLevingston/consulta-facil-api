package com.consultafacil.application.service.dependent;

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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteDependentServiceTest {

    @Mock DependentRepositoryPort dependentRepository;

    DeleteDependentService service;

    @BeforeEach
    void setUp() {
        service = new DeleteDependentService(dependentRepository, new DependentAccessValidator(dependentRepository));
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
    void delete_validGuardian_deletesDependent() {
        User guardian = patientGuardian();
        Dependent dep = dependent(guardian);
        when(dependentRepository.findById("dep-1")).thenReturn(Optional.of(dep));

        service.execute("dep-1", "user-1");

        verify(dependentRepository).delete(dep);
    }

    @Test
    void delete_wrongGuardian_throwsForbidden() {
        User otherGuardian = User.builder().id("other").role(UserRole.PATIENT).build();
        Dependent dep = dependent(otherGuardian);
        when(dependentRepository.findById("dep-1")).thenReturn(Optional.of(dep));

        assertThatThrownBy(() -> service.execute("dep-1", "user-1"))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }
}
