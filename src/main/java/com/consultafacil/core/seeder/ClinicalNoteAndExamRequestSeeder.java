package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.ClinicalNote;
import com.consultafacil.domain.entity.ExamRequest;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.enums.ExamType;
import com.consultafacil.domain.repository.AppointmentRepository;
import com.consultafacil.domain.repository.ClinicalNoteRepository;
import com.consultafacil.domain.repository.ExamRequestRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClinicalNoteAndExamRequestSeeder {

    private final AppointmentRepository appointmentRepository;
    private final ClinicalNoteRepository clinicalNoteRepository;
    private final ExamRequestRepository examRequestRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public void seed() {
        List<String> diagnoses = List.of("Hipertensão arterial", "Diabetes mellitus tipo 2",
                "Lombalgia crônica", "Ansiedade generalizada", "Gastrite crônica");
        List<String> cids = List.of("I10", "E11", "M54.5", "F41.1", "K29.5");
        List<ExamType> exams = List.of(ExamType.HEMOGRAMA_COMPLETO, ExamType.GLICEMIA_JEJUM,
                ExamType.ELETROCARDIOGRAMA, ExamType.RAIO_X, ExamType.ULTRASSOM_ABDOMINAL,
                ExamType.TSH, ExamType.COLESTEROL_TOTAL);
        List<String> prescriptions = List.of(
                "Losartana 50mg 1x/dia", "Metformina 850mg 2x/dia",
                "Omeprazol 20mg em jejum", "Paracetamol 750mg se necessário");

        int notes = 0, examsCreated = 0;
        var completedAppointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .toList();

        for (var appointment : completedAppointments) {
            try {
                if (clinicalNoteRepository.findByAppointmentId(appointment.getId()).isEmpty()) {
                    int idx = faker.random().nextInt(diagnoses.size());
                    clinicalNoteRepository.save(ClinicalNote.builder()
                            .appointment(appointment)
                            .clinicalNotes(faker.lorem().paragraph(2))
                            .diagnosis(diagnoses.get(idx))
                            .diagnosisCid(cids.get(idx))
                            .prescription(prescriptions.get(faker.random().nextInt(prescriptions.size())))
                            .treatmentPlan(faker.lorem().sentence(10))
                            .followUpInstructions("Retornar em " + faker.random().nextInt(1, 6) + " meses.")
                            .build());
                    notes++;
                }
            } catch (Exception e) {
                log.debug("Erro ao criar clinical note: {}", e.getMessage());
            }

            if (faker.random().nextInt(100) < 40) {
                int count = faker.random().nextInt(1, 4);
                for (int i = 0; i < count; i++) {
                    try {
                        ExamRequestStatus examStatus = faker.random().nextInt(100) < 60
                                ? ExamRequestStatus.UPLOADED : ExamRequestStatus.PENDING;
                        examRequestRepository.save(ExamRequest.builder()
                                .appointment(appointment)
                                .professional(appointment.getProfessional())
                                .patient(appointment.getPatient())
                                .examName(exams.get(faker.random().nextInt(exams.size())))
                                .instructions("Realizar em jejum de 8 horas.")
                                .status(examStatus)
                                .professionalNotes(faker.lorem().sentence())
                                .build());
                        examsCreated++;
                    } catch (Exception e) {
                        log.debug("Erro ao criar exam request: {}", e.getMessage());
                    }
                }
            }
        }
        log.info("[Seed] ClinicalNotes: {}, ExamRequests: {}", notes, examsCreated);
    }
}
