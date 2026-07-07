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
public class AppointmentBulkSeeder {

    private final AppointmentRepository appointmentRepository;
    private final ScheduleAppointmentUseCase scheduleAppointmentUseCase;
    private final AppointmentFakeDataHelper fakeDataHelper;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public void seed(List<String> patientUserIds, List<String> professionalProfileIds) {
        List<AppointmentStatus> statusPool = List.of(AppointmentStatus.COMPLETED, AppointmentStatus.COMPLETED,
                AppointmentStatus.COMPLETED, AppointmentStatus.CONFIRMED, AppointmentStatus.CONFIRMED,
                AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING, AppointmentStatus.PENDING,
                AppointmentStatus.CANCELED);
        List<String> reasons = List.of("Consulta de rotina", "Retorno médico", "Dor de cabeça frequente",
                "Check-up anual", "Avaliação clínica", "Exames laboratoriais", "Acompanhamento psicológico",
                "Dor nas costas", "Febre persistente", "Pressão alta", "Ansiedade", "Consulta preventiva",
                "Avaliação cardíaca", "Problemas respiratórios");
        int totalAppointments = 0;
        for (String userId : patientUserIds) {
            int appointmentsPerPatient = faker.random().nextInt(5, 13);
            for (int i = 0; i < appointmentsPerPatient; i++) {
                String professionalId = professionalProfileIds.get(faker.random().nextInt(professionalProfileIds.size()));
                AppointmentStatus status = statusPool.get(faker.random().nextInt(statusPool.size()));
                LocalDateTime scheduledAt = fakeDataHelper.resolveScheduledAt(status);
                try {
                    LocalDateTime safeDate = LocalDateTime.now().plusDays(faker.random().nextInt(1, 20))
                            .withHour(10).withMinute(0).withSecond(0).withNano(0);
                    var dto = CreateAppointmentDTO.builder().professionalId(professionalId).scheduledAt(safeDate)
                            .reason(reasons.get(faker.random().nextInt(reasons.size())))
                            .notes(faker.lorem().sentence(12)).build();
                    var response = scheduleAppointmentUseCase.execute(new ScheduleAppointmentCommand(
                            userId, dto.getProfessionalId(), dto.getScheduledAt(), dto.getReason(), dto.getNotes(),
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
                    totalAppointments++;
                } catch (Exception e) {
                    log.debug("Erro ao criar consulta fake: {}", e.getMessage());
                }
            }
        }
        log.info("Total de consultas criadas: {}", totalAppointments);
    }
}
