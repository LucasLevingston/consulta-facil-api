package com.consultafacil.application.service.insurance;

import com.consultafacil.application.port.in.insurance.RemoveProfessionalInsuranceUseCase;
import com.consultafacil.domain.port.out.insurance.ProfessionalInsuranceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RemoveProfessionalInsuranceService implements RemoveProfessionalInsuranceUseCase {

    private final ProfessionalInsuranceRepositoryPort professionalInsuranceRepository;

    @Override
    public void execute(String professionalId, String planId) {
        professionalInsuranceRepository.deleteByProfessionalIdAndPlanId(professionalId, planId);
    }
}
