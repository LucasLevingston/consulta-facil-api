package com.consultafacil.application.service.insurance;

import com.consultafacil.api.dto.insurance.HealthInsurancePlanDTO;
import com.consultafacil.api.dto.insurance.ProfessionalInsuranceDTO;
import com.consultafacil.application.port.in.insurance.GetProfessionalInsuranceUseCase;
import com.consultafacil.domain.port.out.insurance.ProfessionalInsuranceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetProfessionalInsuranceService implements GetProfessionalInsuranceUseCase {

    private final ProfessionalInsuranceRepositoryPort professionalInsuranceRepository;

    @Override
    public List<ProfessionalInsuranceDTO> execute(String professionalId) {
        return professionalInsuranceRepository.findByProfessionalId(professionalId).stream()
                .map(entry -> {
                    HealthInsurancePlanDTO planDTO = entry.getPlan() != null
                            ? new HealthInsurancePlanDTO(
                                    entry.getPlan().getId(),
                                    entry.getPlan().getName(),
                                    entry.getPlan().getCode(),
                                    entry.getPlan().isActive())
                            : null;
                    return new ProfessionalInsuranceDTO(planDTO, entry.getNotes());
                })
                .toList();
    }
}
