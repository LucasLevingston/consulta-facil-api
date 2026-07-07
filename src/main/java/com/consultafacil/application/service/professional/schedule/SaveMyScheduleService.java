package com.consultafacil.application.service.professional.schedule;

import com.consultafacil.api.dto.schedule.CreateProfessionalScheduleDTO;
import com.consultafacil.api.dto.schedule.ProfessionalScheduleResponseDTO;
import com.consultafacil.application.port.in.professional.schedule.SaveMyScheduleUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.ProfessionalSchedule;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.professional.schedule.ProfessionalScheduleRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveMyScheduleService implements SaveMyScheduleUseCase {

    private final ProfessionalScheduleRepositoryPort scheduleRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalScheduleMapper mapper;

    @Override
    @CacheEvict(value = "professional-schedule", allEntries = true)
    @Transactional
    public List<ProfessionalScheduleResponseDTO> execute(String userId, List<CreateProfessionalScheduleDTO> dtos) {
        log.debug("Saving schedule for user: {}", userId);
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile not found for user: " + userId));

        List<ProfessionalSchedule> saved = dtos.stream().map(dto -> {
            ProfessionalSchedule schedule = scheduleRepository
                    .findByProfessionalIdAndDayOfWeek(profile.getId(), dto.getDayOfWeek())
                    .orElse(ProfessionalSchedule.builder()
                            .professional(profile)
                            .dayOfWeek(dto.getDayOfWeek())
                            .build());

            schedule.setStartTime(dto.getStartTime());
            schedule.setEndTime(dto.getEndTime());
            if (dto.getConsultationDurationMinutes() != null) {
                schedule.setConsultationDurationMinutes(dto.getConsultationDurationMinutes());
            }
            if (dto.getBreakBetweenConsultationsMinutes() != null) {
                schedule.setBreakBetweenConsultationsMinutes(dto.getBreakBetweenConsultationsMinutes());
            }
            if (dto.getIsActive() != null) {
                schedule.setIsActive(dto.getIsActive());
            }
            return scheduleRepository.save(schedule);
        }).toList();

        return saved.stream().map(mapper::toDTO).toList();
    }
}
