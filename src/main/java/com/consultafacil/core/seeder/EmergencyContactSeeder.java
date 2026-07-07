package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.repository.EmergencyContactRepository;
import com.consultafacil.domain.repository.PatientProfileRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmergencyContactSeeder {

    private final PatientProfileRepository patientProfileRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public void seed(String patientUserId, List<String> randomPatientIds) {
        List<String> allPatientIds = new ArrayList<>();
        if (patientUserId != null) allPatientIds.add(patientUserId);
        allPatientIds.addAll(randomPatientIds);

        int created = 0;
        for (String userId : allPatientIds) {
            try {
                var profile = patientProfileRepository.findByUserId(userId).orElse(null);
                if (profile == null) continue;
                if (!emergencyContactRepository.findByPatientProfileId(profile.getId()).isEmpty()) continue;
                emergencyContactRepository.save(EmergencyContact.builder()
                        .patientProfile(profile)
                        .name(faker.name().fullName())
                        .phone(faker.phoneNumber().cellPhone())
                        .build());
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar contato de emergência: {}", e.getMessage());
            }
        }
        log.info("[Seed] Contatos de emergência criados: {}", created);
    }
}
