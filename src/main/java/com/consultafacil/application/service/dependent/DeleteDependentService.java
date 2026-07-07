package com.consultafacil.application.service.dependent;

import com.consultafacil.application.port.in.dependent.DeleteDependentUseCase;
import com.consultafacil.domain.entity.Dependent;
import com.consultafacil.domain.port.out.dependent.DependentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteDependentService implements DeleteDependentUseCase {

    private final DependentRepositoryPort dependentRepository;
    private final DependentAccessValidator accessValidator;

    @Override
    @Transactional
    public void execute(String dependentId, String guardianUserId) {
        Dependent dependent = accessValidator.findOwnedDependent(dependentId, guardianUserId);
        dependentRepository.delete(dependent);
    }
}
