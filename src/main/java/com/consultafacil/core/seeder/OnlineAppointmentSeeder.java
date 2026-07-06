package com.consultafacil.core.seeder;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.application.port.in.ScheduleAppointmentUseCase;
import com.consultafacil.application.port.in.command.ScheduleAppointmentCommand;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnlineAppointmentSeeder {

    private final AppointmentRepository appointmentRepository;
    private final ScheduleAppointmentUseCase scheduleAppointmentUseCase;

    public void seed(String patientUserId, String testProfessionalProfileId) {
        try {
            CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                    .professionalId(testProfessionalProfileId)
                    .scheduledAt(LocalDateTime.now().plusDays(3).withHour(16).withMinute(0).withSecond(0).withNano(0))
                    .reason("Teleconsulta — acompanhamento")
                    .notes("Consulta online de acompanhamento.")
                    .build();
            var onlineConfirmed = scheduleAppointmentUseCase.execute(new ScheduleAppointmentCommand(
                    patientUserId, dto.getProfessionalId(), dto.getScheduledAt(), dto.getReason(), dto.getNotes(),
                    dto.getModality(), dto.getServiceId(), dto.getChosenPaymentMethod()));
            appointmentRepository.findById(onlineConfirmed.getId()).ifPresent(a -> {
                a.setStatus(AppointmentStatus.CONFIRMED);
                a.setModality(AppointmentModality.ONLINE);
                a.setMeetLink("https://meet.google.com/abc-defg-hij");
                appointmentRepository.save(a);
            });
        } catch (Exception e) {
            log.debug("Erro ao criar consulta online confirmada: {}", e.getMessage());
        }

        try {
            CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                    .professionalId(testProfessionalProfileId)
                    .scheduledAt(LocalDateTime.now().plusDays(12).withHour(9).withMinute(30).withSecond(0).withNano(0))
                    .reason("Teleconsulta — primeira consulta")
                    .notes("Paciente solicita atendimento online.")
                    .build();
            var onlinePending = scheduleAppointmentUseCase.execute(new ScheduleAppointmentCommand(
                    patientUserId, dto.getProfessionalId(), dto.getScheduledAt(), dto.getReason(), dto.getNotes(),
                    dto.getModality(), dto.getServiceId(), dto.getChosenPaymentMethod()));
            appointmentRepository.findById(onlinePending.getId()).ifPresent(a -> {
                a.setModality(AppointmentModality.ONLINE);
                appointmentRepository.save(a);
            });
        } catch (Exception e) {
            log.debug("Erro ao criar consulta online pendente: {}", e.getMessage());
        }
    }
}
