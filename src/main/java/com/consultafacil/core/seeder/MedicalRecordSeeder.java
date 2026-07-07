package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.MedicalRecord;
import com.consultafacil.domain.repository.MedicalRecordRepository;
import com.consultafacil.domain.repository.PatientProfileRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicalRecordSeeder {

    private final PatientProfileRepository patientProfileRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public void seed(List<String> patientUserIds) {
        List<String> allergies = List.of("Penicilina", "Dipirona", "Látex", "Frutos do mar", "Nenhuma");
        List<String> medications = List.of("Losartana 50mg", "Metformina 850mg", "Omeprazol 20mg", "Nenhum");
        List<String> familyHistory = List.of(
                "Hipertensão arterial, diabetes tipo 2", "Cardiopatia isquêmica",
                "Sem histórico relevante", "Câncer de mama na mãe");
        List<String> pastHistory = List.of(
                "Apendicectomia em 2015", "Fratura de fêmur em 2018",
                "Sem cirurgias anteriores", "Pneumonia em 2020");

        int created = 0;
        for (String userId : patientUserIds) {
            try {
                patientProfileRepository.findByUserId(userId).ifPresent(profile -> {
                    if (medicalRecordRepository.findByPatientProfileId(profile.getId()).isPresent()) return;
                    medicalRecordRepository.save(MedicalRecord.builder()
                            .patientProfile(profile)
                            .allergies(allergies.get(faker.random().nextInt(allergies.size())))
                            .currentMedication(medications.get(faker.random().nextInt(medications.size())))
                            .familyMedicalHistory(familyHistory.get(faker.random().nextInt(familyHistory.size())))
                            .pastMedicalHistory(pastHistory.get(faker.random().nextInt(pastHistory.size())))
                            .privacyConsent(true)
                            .treatmentConsent(true)
                            .disclosureConsent(faker.bool().bool())
                            .build());
                });
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar medical record: {}", e.getMessage());
            }
        }
        log.info("[Seed] MedicalRecords criados: {}", created);
    }
}
