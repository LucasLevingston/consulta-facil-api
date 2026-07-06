package com.consultafacil.application.service;

import com.consultafacil.api.dto.dependent.DependentResponseDTO;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDependentsByGuardianServiceTest {

    @Mock DependentRepositoryPort dependentRepository;

    ListDependentsByGuardianService service;

    @BeforeEach
    void setUp() {
        service = new ListDependentsByGuardianService(dependentRepository, new DependentMapper());
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
    void listByGuardian_returnsOnlyGuardianDependents() {
        User guardian = patientGuardian();
        when(dependentRepository.findByGuardianId("user-1"))
                .thenReturn(List.of(dependent(guardian)));

        List<DependentResponseDTO> result = service.execute("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Ana Silva");
    }
}
