package com.consultafacil.core.seeder.appointment;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.application.port.in.appointment.ScheduleAppointmentUseCase;
import com.consultafacil.application.port.in.command.ScheduleAppointmentCommand;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.repository.appointment.AppointmentRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkProfessionalAppointmentSeeder {

    private final AppointmentRepository appointmentRepository;
    private final ScheduleAppointmentUseCase scheduleAppointmentUseCase;
    private final AppointmentFakeDataHelper fakeDataHelper;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public void seed(String testProfessionalProfileId, List<String> patientUserIds) {
        List<AppointmentStatus> statuses = List.of(
                AppointmentStatus.COMPLETED, AppointmentStatus.CONFIRMED,
                AppointmentStatus.PENDING, AppointmentStatus.CANCELED);
        List<String> reasons = List.of("Consulta de rotina", "Check-up anual", "Dor de cabeça",
                "Pressão alta", "Retorno médico", "Exames laboratoriais",
                "Avaliação clínica", "Consulta preventiva");

        int created = 0;
        for (int i = 0; i < 100; i++) {
            try {
                String patientId = patientUserIds.get(faker.random().nextInt(patientUserIds.size()));
                AppointmentStatus status = statuses.get(faker.random().nextInt(statuses.size()));
                LocalDateTime scheduledAt = fakeDataHelper.resolveScheduledAt(status);
                LocalDateTime safeDate = LocalDateTime.now().plusDays(1)
                        .withHour(10).withMinute(0).withSecond(0).withNano(0);
                CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                        .professionalId(testProfessionalProfileId)
                        .scheduledAt(safeDate)
                        .reason(reasons.get(faker.random().nextInt(reasons.size())))
                        .notes(faker.lorem().sentence(10))
                        .build();
                var response = scheduleAppointmentUseCase.execute(new ScheduleAppointmentCommand(
                        patientId, dto.getProfessionalId(), dto.getScheduledAt(), dto.getReason(), dto.getNotes(),
                        dto.getModality(), dto.getServiceId(), dto.getChosenPaymentMethod()));
                final AppointmentStatus finalStatus = status;
                final LocalDateTime finalScheduledAt = scheduledAt;
                appointmentRepository.findById(response.getId()).ifPresent(appointment -> {
                    appointment.setStatus(finalStatus);
                    appointment.setScheduledAt(finalScheduledAt);
                    if (finalStatus == AppointmentStatus.COMPLETED) {
                        fakeDataHelper.enrichCompletedAppointment(appointment);
                    }
                    appointmentRepository.save(appointment);
                });
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar consulta fake para profissional teste: {}", e.getMessage());
            }
        }
        log.info("Criadas {} consultas para o profissional de teste", created);
    }
}
