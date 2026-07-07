package com.consultafacil.adapter.out.persistence.dependent;

import com.consultafacil.domain.entity.Dependent;
import com.consultafacil.domain.port.out.dependent.DependentRepositoryPort;
import com.consultafacil.domain.repository.dependent.DependentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DependentRepositoryAdapter implements DependentRepositoryPort {

    private final DependentRepository dependentRepository;

    @Override
    public Dependent save(Dependent dependent) {
        return dependentRepository.save(dependent);
    }

    @Override
    public List<Dependent> findByGuardianId(String guardianUserId) {
        return dependentRepository.findByGuardianId(guardianUserId);
    }

    @Override
    public Optional<Dependent> findById(String id) {
        return dependentRepository.findById(id);
    }

    @Override
    public void delete(Dependent dependent) {
        dependentRepository.delete(dependent);
    }
}
