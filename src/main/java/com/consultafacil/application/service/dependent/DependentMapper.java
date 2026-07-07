package com.consultafacil.application.service.dependent;

import com.consultafacil.api.dto.dependent.DependentResponseDTO;
import com.consultafacil.domain.entity.Dependent;
import org.springframework.stereotype.Component;

@Component
public class DependentMapper {

    public DependentResponseDTO toDTO(Dependent d) {
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
