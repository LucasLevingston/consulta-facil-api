package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.ProfessionalService;
import com.example.consulta.domain.port.out.ProfessionalServiceRepositoryPort;
import com.example.consulta.domain.repository.ProfessionalServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfessionalServiceRepositoryAdapter implements ProfessionalServiceRepositoryPort {

    private final ProfessionalServiceRepository professionalServiceRepository;

    @Override
    public ProfessionalService save(ProfessionalService service) {
        return professionalServiceRepository.save(service);
    }

    @Override
    public Optional<ProfessionalService> findById(String id) {
        return professionalServiceRepository.findById(id);
    }

    @Override
    public List<ProfessionalService> findByProfessionalIdAndActiveTrue(String professionalId) {
        return professionalServiceRepository.findByProfessionalIdAndActiveTrue(professionalId);
    }
}
