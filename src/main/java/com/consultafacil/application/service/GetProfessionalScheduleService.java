package com.consultafacil.application.service;

import com.consultafacil.api.dto.schedule.ProfessionalScheduleResponseDTO;
import com.consultafacil.application.port.in.GetProfessionalScheduleUseCase;
import com.consultafacil.domain.port.out.ProfessionalScheduleRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetProfessionalScheduleService implements GetProfessionalScheduleUseCase {

    private final ProfessionalScheduleRepositoryPort scheduleRepository;
    private final ProfessionalScheduleMapper mapper;

    @Override
    @Cacheable(value = "professional-schedule", key = "#professionalId")
    @Transactional(readOnly = true)
    public List<ProfessionalScheduleResponseDTO> execute(String professionalId) {
        log.debug("Fetching schedule for professional: {}", professionalId);
        return scheduleRepository.findByProfessionalId(professionalId)
                .stream().map(mapper::toDTO).toList();
    }
}
