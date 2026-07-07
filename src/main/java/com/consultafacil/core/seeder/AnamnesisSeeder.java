package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.MedicalHistory;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.repository.AppointmentRepository;
import com.consultafacil.domain.repository.MedicalHistoryRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnamnesisSeeder {

    private final AppointmentRepository appointmentRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public void seed() {
        List<String> complaints = List.of(
                "Dor de cabeça frequente há 2 semanas",
                "Cansaço excessivo e falta de ar ao esforço",
                "Dor abdominal pós-refeição",
                "Pressão alta detectada em medição domiciliar",
                "Ansiedade e dificuldade para dormir",
                "Dor nas costas há 3 meses",
                "Tosse persistente há 10 dias");
        List<String> meds = List.of("Losartana 50mg", "Metformina 850mg", "Nenhum",
                "Omeprazol 20mg", "Dipirona 500mg se necessário");
        List<String> allergies = List.of("Nenhuma conhecida", "Penicilina", "Dipirona",
                "Látex", "Frutos do mar");
        List<String> histories = List.of(
                "Hipertensão diagnosticada em 2018",
                "Diabetes tipo 2 desde 2020",
                "Sem histórico relevante",
                "Pneumonia em 2019, apendicectomia em 2015");

        int created = 0;
        var completedAppointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .toList();

        for (var appointment : completedAppointments) {
            try {
                if (medicalHistoryRepository.findByAppointmentId(appointment.getId()).isPresent()) continue;
                if (faker.random().nextInt(100) < 60) {
                    medicalHistoryRepository.save(MedicalHistory.builder()
                            .appointment(appointment)
                            .chiefComplaint(complaints.get(faker.random().nextInt(complaints.size())))
                            .currentMedications(meds.get(faker.random().nextInt(meds.size())))
                            .allergies(allergies.get(faker.random().nextInt(allergies.size())))
                            .medicalHistory(histories.get(faker.random().nextInt(histories.size())))
                            .familyHistory(faker.bool().bool()
                                    ? "Hipertensão e diabetes na família"
                                    : "Sem histórico familiar relevante")
                            .observations(faker.lorem().sentence())
                            .build());
                    created++;
                }
            } catch (Exception e) {
                log.debug("Erro ao criar anamnese: {}", e.getMessage());
            }
        }
        log.info("[Seed] Anamneses criadas: {}", created);
    }
}
