package com.consultafacil.core.seeder;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.application.port.in.ScheduleAppointmentUseCase;
import com.consultafacil.application.port.in.command.ScheduleAppointmentCommand;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.repository.AppointmentRepository;
import com.consultafacil.domain.repository.PatientProfileRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestAppointmentSeeder {

    private final PatientProfileRepository patientProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final ScheduleAppointmentUseCase scheduleAppointmentUseCase;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public void seed(String patientUserId, String testProfessionalProfileId) {
        PatientProfile profile = patientProfileRepository.findByUserId(patientUserId).orElse(null);
        if (profile == null) return;

        String[] reasons = { "Consulta de rotina", "Dor no peito", "Check-up anual", "Pressão alta", "Retorno" };
        int[] daysAhead = { -10, -5, 1, 7, 20 };
        int[] hours = { 9, 11, 14, 10, 15 };
        AppointmentStatus[] statuses = {
                AppointmentStatus.COMPLETED, AppointmentStatus.COMPLETED,
                AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING, AppointmentStatus.PENDING
        };

        for (int i = 0; i < reasons.length; i++) {
            try {
                CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                        .professionalId(testProfessionalProfileId)
                        .scheduledAt(LocalDateTime.now().plusDays(daysAhead[i])
                                .withHour(hours[i]).withMinute(0).withSecond(0).withNano(0))
                        .reason(reasons[i])
                        .notes(faker.lorem().sentence())
                        .build();
                var response = scheduleAppointmentUseCase.execute(new ScheduleAppointmentCommand(
                        patientUserId, dto.getProfessionalId(), dto.getScheduledAt(), dto.getReason(), dto.getNotes(),
                        dto.getModality(), dto.getServiceId(), dto.getChosenPaymentMethod()));
                final AppointmentStatus finalStatus = statuses[i];
                appointmentRepository.findById(response.getId()).ifPresent(appointment -> {
                    appointment.setStatus(finalStatus);
                    if (finalStatus == AppointmentStatus.COMPLETED) {
                        appointment.setPaymentStatus(AppointmentPaymentStatus.PAID);
                        appointment.setPaymentAmount(BigDecimal.valueOf(250));
                    }
                    appointmentRepository.save(appointment);
                });
            } catch (Exception e) {
                log.debug("Erro ao criar consulta de teste: {}", e.getMessage());
            }
        }
    }
}
