package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.ExamLab;
import com.consultafacil.domain.entity.ExamLabHours;
import com.consultafacil.domain.repository.examlab.ExamLabHoursRepository;
import com.consultafacil.domain.repository.examlab.ExamLabRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExamLabSeeder {

    private final ExamLabRepository examLabRepository;
    private final ExamLabHoursRepository examLabHoursRepository;
    private final ExamLabDataProvider dataProvider;

    public void seed() {
        int created = 0;
        for (ExamLabDataProvider.LabDef def : dataProvider.getLabs()) {
            try {
                boolean exists = examLabRepository.findByStatus("ACTIVE").stream()
                        .anyMatch(l -> l.getName().equals(def.name()));
                if (exists) continue;

                ExamLab lab = examLabRepository.save(ExamLab.builder()
                        .name(def.name())
                        .description(def.description())
                        .phone(def.phone())
                        .address(def.address())
                        .city(def.city())
                        .state(def.state())
                        .latitude(def.lat())
                        .longitude(def.lng())
                        .imageUrl(def.imageUrl())
                        .acceptedExams(new ArrayList<>(def.acceptedExams()))
                        .status("ACTIVE")
                        .build());

                for (ExamLabDataProvider.DaySlot slot : dataProvider.getWeekdays()) {
                    examLabHoursRepository.save(ExamLabHours.builder()
                            .examLab(lab)
                            .dayOfWeek(slot.day())
                            .openTime(slot.open())
                            .closeTime(slot.close())
                            .slotDurationMinutes(slot.duration())
                            .isOpen(slot.isOpen())
                            .build());
                }
                created++;
            } catch (Exception e) {
                log.warn("Erro ao criar exam lab {}: {}", def.name(), e.getMessage());
            }
        }
        log.info("[Seed] ExamLabs criados: {}", created);
    }
}
