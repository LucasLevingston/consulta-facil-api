package com.consultafacil.application.service.dependent;

import com.consultafacil.api.dto.dependent.CreateDependentDTO;
import com.consultafacil.api.dto.dependent.DependentResponseDTO;
import com.consultafacil.application.port.in.CreateDependentUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Dependent;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.DependentRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CreateDependentService implements CreateDependentUseCase {

    private final DependentRepositoryPort dependentRepository;
    private final UserRepositoryPort userRepository;
    private final DependentMapper mapper;

    @Override
    @Transactional
    public DependentResponseDTO execute(String guardianUserId, CreateDependentDTO dto) {
        User guardian = userRepository.findById(guardianUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", guardianUserId));
        if (guardian.getRole() != UserRole.PATIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas pacientes podem cadastrar dependentes");
        }
        Dependent dependent = Dependent.builder()
                .guardian(guardian)
                .name(dto.name())
                .cpf(dto.cpf())
                .birthDate(dto.birthDate())
                .gender(dto.gender())
                .relationship(dto.relationship())
                .build();
        return mapper.toDTO(dependentRepository.save(dependent));
    }
}
