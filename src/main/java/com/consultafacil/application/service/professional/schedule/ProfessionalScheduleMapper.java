package com.consultafacil.application.service.professional.schedule;

import com.consultafacil.api.dto.schedule.ProfessionalScheduleResponseDTO;
import com.consultafacil.domain.entity.ProfessionalSchedule;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalScheduleMapper {

    public ProfessionalScheduleResponseDTO toDTO(ProfessionalSchedule schedule) {
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
