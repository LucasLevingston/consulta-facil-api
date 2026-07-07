package com.consultafacil.application.service.dependent;

import com.consultafacil.api.dto.dependent.DependentResponseDTO;
import com.consultafacil.api.dto.dependent.UpdateDependentDTO;
import com.consultafacil.application.port.in.UpdateDependentUseCase;
import com.consultafacil.domain.entity.Dependent;
import com.consultafacil.domain.port.out.DependentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateDependentService implements UpdateDependentUseCase {

    private final DependentRepositoryPort dependentRepository;
    private final DependentAccessValidator accessValidator;
    private final DependentMapper mapper;

    @Override
    @Transactional
    public DependentResponseDTO execute(String dependentId, String guardianUserId, UpdateDependentDTO dto) {
        Dependent dependent = accessValidator.findOwnedDependent(dependentId, guardianUserId);
        if (dto.name() != null) dependent.setName(dto.name());
        if (dto.cpf() != null) dependent.setCpf(dto.cpf());
        if (dto.birthDate() != null) dependent.setBirthDate(dto.birthDate());
        if (dto.gender() != null) dependent.setGender(dto.gender());
        if (dto.relationship() != null) dependent.setRelationship(dto.relationship());
        return mapper.toDTO(dependentRepository.save(dependent));
    }
}
