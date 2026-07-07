package com.consultafacil.application.service;

import com.consultafacil.api.dto.schedule.ProfessionalScheduleResponseDTO;
import com.consultafacil.application.port.in.GetMyScheduleUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalScheduleRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetMyScheduleService implements GetMyScheduleUseCase {

    private final ProfessionalScheduleRepositoryPort scheduleRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalScheduleMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProfessionalScheduleResponseDTO> execute(String userId) {
        log.debug("Fetching schedule for user: {}", userId);
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile not found for user: " + userId));
        return scheduleRepository.findByProfessionalId(profile.getId())
                .stream().map(mapper::toDTO).toList();
    }
}
