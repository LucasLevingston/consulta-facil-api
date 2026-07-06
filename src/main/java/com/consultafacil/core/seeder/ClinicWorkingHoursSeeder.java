package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.ClinicWorkingHours;
import com.consultafacil.domain.repository.ClinicRepository;
import com.consultafacil.domain.repository.ClinicWorkingHoursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClinicWorkingHoursSeeder {

    private final ClinicRepository clinicRepository;
    private final ClinicWorkingHoursRepository clinicWorkingHoursRepository;

    private record DaySlot(String day, LocalTime open, LocalTime close, boolean isOpen) {
    }

    public void seed() {
        List<DaySlot> slots = List.of(
                new DaySlot("MONDAY", LocalTime.of(8, 0), LocalTime.of(18, 0), true),
                new DaySlot("TUESDAY", LocalTime.of(8, 0), LocalTime.of(18, 0), true),
                new DaySlot("WEDNESDAY", LocalTime.of(8, 0), LocalTime.of(18, 0), true),
                new DaySlot("THURSDAY", LocalTime.of(8, 0), LocalTime.of(18, 0), true),
                new DaySlot("FRIDAY", LocalTime.of(8, 0), LocalTime.of(17, 0), true),
                new DaySlot("SATURDAY", LocalTime.of(8, 0), LocalTime.of(12, 0), false),
                new DaySlot("SUNDAY", LocalTime.of(8, 0), LocalTime.of(12, 0), false));

        int created = 0;
        for (Clinic clinic : clinicRepository.findAll()) {
            for (DaySlot slot : slots) {
                try {
                    if (clinicWorkingHoursRepository.findByClinicIdAndDayOfWeek(clinic.getId(), slot.day()).isEmpty()) {
                        clinicWorkingHoursRepository.save(ClinicWorkingHours.builder()
                                .clinic(clinic)
                                .dayOfWeek(slot.day())
                                .openTime(slot.open())
                                .closeTime(slot.close())
                                .isOpen(slot.isOpen())
                                .build());
                        created++;
                    }
                } catch (Exception e) {
                    log.debug("Erro ao criar working hours: {}", e.getMessage());
                }
            }
        }
        log.info("[Seed] ClinicWorkingHours criados: {}", created);
    }
}
