package com.consultafacil.application.service.insurance;

import com.consultafacil.api.dto.insurance.HealthInsurancePlanDTO;
import com.consultafacil.api.dto.insurance.PatientInsuranceDTO;
import com.consultafacil.application.port.in.insurance.GetPatientInsuranceUseCase;
import com.consultafacil.domain.port.out.insurance.PatientInsuranceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPatientInsuranceService implements GetPatientInsuranceUseCase {

    private final PatientInsuranceRepositoryPort patientInsuranceRepository;

    @Override
    public List<PatientInsuranceDTO> execute(String patientId) {
        return patientInsuranceRepository.findByPatientId(patientId).stream()
                .map(entry -> {
                    HealthInsurancePlanDTO planDTO = entry.getPlan() != null
                            ? new HealthInsurancePlanDTO(
                                    entry.getPlan().getId(),
                                    entry.getPlan().getName(),
                                    entry.getPlan().getCode(),
                                    entry.getPlan().isActive())
                            : null;
                    return new PatientInsuranceDTO(
                            entry.getId(),
                            planDTO,
                            entry.getMemberNumber(),
                            entry.getHolderName(),
                            entry.getValidUntil());
                })
                .toList();
    }
}
