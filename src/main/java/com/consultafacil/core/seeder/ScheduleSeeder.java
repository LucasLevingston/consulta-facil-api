package com.consultafacil.core.seeder;

import com.consultafacil.application.port.in.professional.schedule.SaveMyScheduleUseCase;
import com.consultafacil.domain.repository.professional.profile.ProfessionalProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleSeeder {

    private final SaveMyScheduleUseCase saveMyScheduleUseCase;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final ScheduleTemplateProvider scheduleTemplateProvider;

    public void seedSchedule(String userId, ScheduleTemplate template) {
        if (userId == null) return;
        try {
            var dtos = scheduleTemplateProvider.buildScheduleDTOs(template);
            saveMyScheduleUseCase.execute(userId, dtos);
            log.info("[Seed] Schedule seeded for userId={} template={}", userId, template);
        } catch (Exception e) {
            log.warn("[Seed] Failed to seed schedule for userId={}: {}", userId, e.getMessage());
        }
    }

    public void seedForProfessionals(List<String> professionalProfileIds) {
        ScheduleTemplate[] templates = ScheduleTemplate.values();
        int assigned = 0;
        for (int i = 0; i < professionalProfileIds.size(); i++) {
            String profId = professionalProfileIds.get(i);
            String userId = professionalProfileRepository.findById(profId)
                    .map(p -> p.getUser().getId())
                    .orElse(null);
            if (userId == null) continue;
            if (i % 5 == 0 || i % 5 == 1 || i % 5 == 2) {
                seedSchedule(userId, templates[i % templates.length]);
                assigned++;
            }
        }
        log.info("[Seed] Schedules seeded for {}/{} random professionals", assigned, professionalProfileIds.size());
    }
}
