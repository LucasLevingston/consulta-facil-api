package com.consultafacil.application.service.insurance;

import com.consultafacil.api.dto.insurance.AddProfessionalInsuranceRequest;
import com.consultafacil.application.port.in.insurance.AddProfessionalInsuranceUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalInsuranceAccepted;
import com.consultafacil.domain.port.out.insurance.HealthInsurancePlanRepositoryPort;
import com.consultafacil.domain.port.out.insurance.ProfessionalInsuranceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AddProfessionalInsuranceService implements AddProfessionalInsuranceUseCase {

    private final ProfessionalInsuranceRepositoryPort professionalInsuranceRepository;
    private final HealthInsurancePlanRepositoryPort planRepository;

    @Override
    public void execute(String professionalId, AddProfessionalInsuranceRequest request) {
        planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("HealthInsurancePlan", request.planId()));

        ProfessionalInsuranceAccepted entity = ProfessionalInsuranceAccepted.builder()
                .professionalId(professionalId)
                .planId(request.planId())
                .notes(request.notes())
                .build();

        professionalInsuranceRepository.save(entity);
    }
}
