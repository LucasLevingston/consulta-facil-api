package com.consultafacil.application.service.insurance;

import com.consultafacil.api.dto.insurance.HealthInsurancePlanDTO;
import com.consultafacil.api.dto.insurance.PatientInsuranceDTO;
import com.consultafacil.api.dto.insurance.RegisterPatientInsuranceRequest;
import com.consultafacil.application.port.in.insurance.RegisterPatientInsuranceUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.HealthInsurancePlan;
import com.consultafacil.domain.entity.PatientInsurance;
import com.consultafacil.domain.port.out.insurance.HealthInsurancePlanRepositoryPort;
import com.consultafacil.domain.port.out.insurance.PatientInsuranceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterPatientInsuranceService implements RegisterPatientInsuranceUseCase {

    private final PatientInsuranceRepositoryPort patientInsuranceRepository;
    private final HealthInsurancePlanRepositoryPort planRepository;

    @Override
    public PatientInsuranceDTO execute(String patientId, RegisterPatientInsuranceRequest request) {
        HealthInsurancePlan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("HealthInsurancePlan", request.planId()));

        PatientInsurance entity = PatientInsurance.builder()
                .patientId(patientId)
                .plan(plan)
                .memberNumber(request.memberNumber())
                .holderName(request.holderName())
                .validUntil(request.validUntil())
                .build();

        PatientInsurance saved = patientInsuranceRepository.save(entity);

        HealthInsurancePlanDTO planDTO = new HealthInsurancePlanDTO(
                plan.getId(), plan.getName(), plan.getCode(), plan.isActive());

        return new PatientInsuranceDTO(
                saved.getId(),
                planDTO,
                saved.getMemberNumber(),
                saved.getHolderName(),
                saved.getValidUntil());
    }
}
