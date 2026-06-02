package com.example.consulta.application.service;

import com.example.consulta.api.dto.schedule.CreateProfessionalScheduleDTO;
import com.example.consulta.api.dto.schedule.ProfessionalScheduleResponseDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.ProfessionalSchedule;
import com.example.consulta.domain.port.out.ProfessionalProfileRepositoryPort;
import com.example.consulta.application.port.in.ProfessionalScheduleUseCase;
import com.example.consulta.domain.port.out.ProfessionalScheduleRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfessionalScheduleService implements ProfessionalScheduleUseCase {

    private final ProfessionalScheduleRepositoryPort scheduleRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

    @Cacheable(value = "professional-schedule", key = "#professionalId")
    @Transactional(readOnly = true)
    public List<ProfessionalScheduleResponseDTO> getScheduleByProfessionalId(String professionalId) {
        log.debug("Fetching schedule for professional: {}", professionalId);
        return scheduleRepository.findByProfessionalId(professionalId)
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessionalScheduleResponseDTO> getMySchedule(String userId) {
        log.debug("Fetching schedule for user: {}", userId);
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile not found for user: " + userId));
        return scheduleRepository.findByProfessionalId(profile.getId())
                .stream().map(this::toDTO).toList();
    }

    @Override
    @CacheEvict(value = "professional-schedule", allEntries = true)
    @Transactional
    public List<ProfessionalScheduleResponseDTO> saveMySchedule(String userId, List<CreateProfessionalScheduleDTO> dtos) {
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

        return saved.stream().map(this::toDTO).toList();
    }

    @Override
    public List<ProfessionalScheduleResponseDTO> getByProfessionalId(String professionalId) { return getScheduleByProfessionalId(professionalId); }

    private ProfessionalScheduleResponseDTO toDTO(ProfessionalSchedule schedule) {
        return ProfessionalScheduleResponseDTO.builder()
                .id(schedule.getId())
                .professionalProfileId(schedule.getProfessional().getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .consultationDurationMinutes(schedule.getConsultationDurationMinutes())
                .breakBetweenConsultationsMinutes(schedule.getBreakBetweenConsultationsMinutes())
                .isActive(schedule.getIsActive())
                .build();
    }
}
