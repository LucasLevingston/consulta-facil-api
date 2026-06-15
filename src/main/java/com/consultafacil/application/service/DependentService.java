package com.consultafacil.application.service;

import com.consultafacil.api.dto.dependent.CreateDependentDTO;
import com.consultafacil.api.dto.dependent.DependentResponseDTO;
import com.consultafacil.api.dto.dependent.UpdateDependentDTO;
import com.consultafacil.application.port.in.DependentUseCase;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class DependentService implements DependentUseCase {

    private final DependentRepositoryPort dependentRepository;
    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public DependentResponseDTO create(String guardianUserId, CreateDependentDTO dto) {
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
        return toDTO(dependentRepository.save(dependent));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DependentResponseDTO> listByGuardian(String guardianUserId) {
        return dependentRepository.findByGuardianId(guardianUserId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public DependentResponseDTO update(String dependentId, String guardianUserId, UpdateDependentDTO dto) {
        Dependent dependent = findOwnedDependent(dependentId, guardianUserId);
        if (dto.name() != null) dependent.setName(dto.name());
        if (dto.cpf() != null) dependent.setCpf(dto.cpf());
        if (dto.birthDate() != null) dependent.setBirthDate(dto.birthDate());
        if (dto.gender() != null) dependent.setGender(dto.gender());
        if (dto.relationship() != null) dependent.setRelationship(dto.relationship());
        return toDTO(dependentRepository.save(dependent));
    }

    @Override
    @Transactional
    public void delete(String dependentId, String guardianUserId) {
        Dependent dependent = findOwnedDependent(dependentId, guardianUserId);
        dependentRepository.delete(dependent);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Dependent findOwnedDependent(String dependentId, String guardianUserId) {
        Dependent dependent = dependentRepository.findById(dependentId)
                .orElseThrow(() -> new ResourceNotFoundException("Dependent", dependentId));
        if (!dependent.getGuardian().getId().equals(guardianUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }
        return dependent;
    }

    private DependentResponseDTO toDTO(Dependent d) {
        return new DependentResponseDTO(
                d.getId(),
                d.getName(),
                d.getCpf(),
                d.getBirthDate(),
                d.getGender() != null ? d.getGender().name() : null,
                d.getRelationship().name(),
                d.getCreatedAt() != null ? d.getCreatedAt().toString() : null
        );
    }
}
