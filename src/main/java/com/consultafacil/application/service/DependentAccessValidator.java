package com.consultafacil.application.service;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Dependent;
import com.consultafacil.domain.port.out.DependentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class DependentAccessValidator {

    private final DependentRepositoryPort dependentRepository;

    public Dependent findOwnedDependent(String dependentId, String guardianUserId) {
        Dependent dependent = dependentRepository.findById(dependentId)
                .orElseThrow(() -> new ResourceNotFoundException("Dependent", dependentId));
        if (!dependent.getGuardian().getId().equals(guardianUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }
        return dependent;
    }
}
