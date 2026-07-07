package com.consultafacil.core.seeder;

import com.consultafacil.api.dto.schedule.CreateProfessionalScheduleDTO;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
public class ScheduleTemplateProvider {

    private record SlotDef(String day, String start, String end, int duration, int breakMin, boolean active) {
    }

    public List<CreateProfessionalScheduleDTO> buildScheduleDTOs(ScheduleTemplate template) {
        List<SlotDef> slots = switch (template) {
            case FULL_WEEK -> List.of(
                    new SlotDef("MONDAY", "08:00", "17:00", 30, 10, true),
                    new SlotDef("TUESDAY", "08:00", "17:00", 30, 10, true),
                    new SlotDef("WEDNESDAY", "08:00", "17:00", 30, 10, true),
                    new SlotDef("THURSDAY", "08:00", "17:00", 30, 10, true),
                    new SlotDef("FRIDAY", "08:00", "16:00", 30, 10, true),
                    new SlotDef("SATURDAY", "08:00", "12:00", 30, 0, false),
                    new SlotDef("SUNDAY", "08:00", "12:00", 30, 0, false));
            case MORNING_ONLY -> List.of(
                    new SlotDef("MONDAY", "07:00", "12:00", 45, 15, true),
                    new SlotDef("TUESDAY", "07:00", "12:00", 45, 15, true),
                    new SlotDef("WEDNESDAY", "07:00", "12:00", 45, 15, true),
                    new SlotDef("THURSDAY", "07:00", "12:00", 45, 15, true),
                    new SlotDef("FRIDAY", "07:00", "12:00", 45, 15, true),
                    new SlotDef("SATURDAY", "08:00", "12:00", 45, 0, false),
                    new SlotDef("SUNDAY", "08:00", "12:00", 45, 0, false));
            case AFTERNOON_ONLY -> List.of(
                    new SlotDef("MONDAY", "13:00", "18:00", 30, 10, true),
                    new SlotDef("TUESDAY", "13:00", "18:00", 30, 10, true),
                    new SlotDef("WEDNESDAY", "13:00", "18:00", 30, 10, true),
                    new SlotDef("THURSDAY", "13:00", "18:00", 30, 10, true),
                    new SlotDef("FRIDAY", "13:00", "17:30", 30, 10, true),
                    new SlotDef("SATURDAY", "08:00", "12:00", 30, 0, false),
                    new SlotDef("SUNDAY", "08:00", "12:00", 30, 0, false));
            case THREE_DAYS -> List.of(
                    new SlotDef("MONDAY", "08:00", "12:00", 60, 15, true),
                    new SlotDef("TUESDAY", "08:00", "12:00", 60, 15, false),
                    new SlotDef("WEDNESDAY", "14:00", "19:00", 60, 15, true),
                    new SlotDef("THURSDAY", "08:00", "12:00", 60, 15, false),
                    new SlotDef("FRIDAY", "14:00", "19:00", 60, 15, true),
                    new SlotDef("SATURDAY", "08:00", "12:00", 60, 0, false),
                    new SlotDef("SUNDAY", "08:00", "12:00", 60, 0, false));
        };

        return slots.stream().map(s -> CreateProfessionalScheduleDTO.builder()
                .dayOfWeek(s.day())
                .startTime(LocalTime.parse(s.start()))
                .endTime(LocalTime.parse(s.end()))
                .consultationDurationMinutes(s.duration())
                .breakBetweenConsultationsMinutes(s.breakMin())
                .isActive(s.active())
                .build()).toList();
    }
}
