package com.consultafacil.application.service.whatsapp;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.appointment.CancelAppointmentUseCase;
import com.consultafacil.application.port.in.appointment.ScheduleAppointmentUseCase;
import com.consultafacil.application.port.in.command.CancelAppointmentCommand;
import com.consultafacil.application.port.in.command.ScheduleAppointmentCommand;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsAppToolExecutorTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock ScheduleAppointmentUseCase scheduleAppointment;
    @Mock CancelAppointmentUseCase cancelAppointment;

    final ObjectMapper objectMapper = new ObjectMapper();
    WhatsAppToolExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new WhatsAppToolExecutor(professionalProfileRepository, patientProfileRepository,
                appointmentRepository, scheduleAppointment, cancelAppointment);
    }

    private JsonNode json(String raw) throws Exception {
        return objectMapper.readTree(raw);
    }

    private ProfessionalProfile professional(String id, String name, Specialty specialty) {
        return ProfessionalProfile.builder().id(id).user(User.builder().name(name).build()).specialty(specialty).build();
    }

    @Test
    void unknownTool_isReportedBackToTheModel() throws Exception {
        assertThat(executor.execute("u1", "delete_everything", json("{}")))
                .isEqualTo("Ferramenta desconhecida: delete_everything");
    }

    @Test
    void searchProfessionals_listsUpToFiveActiveProfessionalsForTheSpecialty() throws Exception {
        Page<ProfessionalProfile> page = new PageImpl<>(List.of(
                professional("p1", "Dra. Ana", Specialty.CARDIOLOGIA),
                professional("p2", "Dr. Beto", null)));
        when(professionalProfileRepository.findActiveWithFilters("", "CARDIOLOGIA", "", PageRequest.of(0, 5)))
                .thenReturn(page);

        String result = executor.execute(null, "search_professionals", json("{\"specialty\":\"CARDIOLOGIA\"}"));

        assertThat(result)
                .startsWith("Profissionais encontrados:")
                .contains("- ID: p1, Nome: Dra. Ana, Especialidade: CARDIOLOGIA")
                .contains("- ID: p2, Nome: Dr. Beto, Especialidade: \n");
    }

    @Test
    void searchProfessionals_withoutResults() throws Exception {
        when(professionalProfileRepository.findActiveWithFilters(eq(""), eq(""), eq(""), any()))
                .thenReturn(Page.empty());

        assertThat(executor.execute(null, "search_professionals", json("{}")))
                .isEqualTo("Nenhum profissional encontrado para a especialidade informada.");
    }

    @Test
    void listAppointments_requiresRegisteredUser() throws Exception {
        assertThat(executor.execute(null, "list_appointments", json("{}")))
                .isEqualTo("Usuário não cadastrado. Acesse o app para se registrar.");
        verifyNoInteractions(patientProfileRepository, appointmentRepository);
    }

    @Test
    void listAppointments_requiresPatientProfile() throws Exception {
        when(patientProfileRepository.findByUserId("u1")).thenReturn(Optional.empty());

        assertThat(executor.execute("u1", "list_appointments", json("{}")))
                .isEqualTo("Perfil de paciente não encontrado.");
    }

    @Test
    void listAppointments_showsOnlyActiveAppointments() throws Exception {
        when(patientProfileRepository.findByUserId("u1"))
                .thenReturn(Optional.of(PatientProfile.builder().id("pat-1").build()));
        ProfessionalProfile doctor = professional("p1", "Dra. Ana", Specialty.CARDIOLOGIA);
        Appointment upcoming = Appointment.builder().id("a1").professional(doctor)
                .scheduledAt(LocalDateTime.of(2026, 12, 1, 14, 30)).status(AppointmentStatus.CONFIRMED).build();
        Appointment canceled = Appointment.builder().id("a2").professional(doctor)
                .scheduledAt(LocalDateTime.of(2026, 12, 2, 9, 0)).status(AppointmentStatus.CANCELED).build();
        Appointment done = Appointment.builder().id("a3").professional(doctor)
                .scheduledAt(LocalDateTime.of(2026, 10, 2, 9, 0)).status(AppointmentStatus.COMPLETED).build();
        when(appointmentRepository.findByPatientId("pat-1", PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(upcoming, canceled, done)));

        String result = executor.execute("u1", "list_appointments", json("{}"));

        assertThat(result)
                .contains("- ID: a1, Médico: Dra. Ana, Data: 01/12/2026 14:30, Status: CONFIRMED")
                .doesNotContain("a2")
                .doesNotContain("a3");
    }

    @Test
    void listAppointments_withoutAppointments() throws Exception {
        when(patientProfileRepository.findByUserId("u1"))
                .thenReturn(Optional.of(PatientProfile.builder().id("pat-1").build()));
        when(appointmentRepository.findByPatientId(eq("pat-1"), any())).thenReturn(Page.empty());

        assertThat(executor.execute("u1", "list_appointments", json("{}"))).isEqualTo("Nenhuma consulta encontrada.");
    }

    @Test
    void bookAppointment_schedulesInPersonAppointmentForTheSender() throws Exception {
        when(scheduleAppointment.execute(any())).thenReturn(AppointmentResponseDTO.builder()
                .id("new-appt").scheduledAt(LocalDateTime.of(2026, 12, 10, 9, 30)).build());

        String result = executor.execute("u1", "book_appointment",
                json("{\"professional_id\":\"p1\",\"date_time\":\"2026-12-10T09:30\",\"reason\":\"dor no peito\"}"));

        ArgumentCaptor<ScheduleAppointmentCommand> cmd = ArgumentCaptor.forClass(ScheduleAppointmentCommand.class);
        verify(scheduleAppointment).execute(cmd.capture());
        assertThat(cmd.getValue().userId()).isEqualTo("u1");
        assertThat(cmd.getValue().professionalId()).isEqualTo("p1");
        assertThat(cmd.getValue().scheduledAt()).isEqualTo(LocalDateTime.of(2026, 12, 10, 9, 30));
        assertThat(cmd.getValue().reason()).isEqualTo("dor no peito");
        assertThat(cmd.getValue().modality()).isEqualTo(AppointmentModality.IN_PERSON);
        assertThat(result).isEqualTo("Consulta agendada! ID: new-appt, Data: 10/12/2026 09:30");
    }

    @Test
    void bookAppointment_requiresRegisteredUser() throws Exception {
        assertThat(executor.execute(null, "book_appointment", json("{\"professional_id\":\"p1\"}")))
                .isEqualTo("Usuário não cadastrado. Acesse o app para se registrar.");
        verifyNoInteractions(scheduleAppointment);
    }

    @Test
    void bookAppointment_invalidDateIsReportedInsteadOfThrown() throws Exception {
        String result = executor.execute("u1", "book_appointment",
                json("{\"professional_id\":\"p1\",\"date_time\":\"amanhã às 10\"}"));

        assertThat(result).startsWith("Erro ao executar ação:");
        verifyNoInteractions(scheduleAppointment);
    }

    @Test
    void cancelAppointment_usesDefaultReasonWhenNoneGiven() throws Exception {
        String result = executor.execute("u1", "cancel_appointment", json("{\"appointment_id\":\"a1\"}"));

        ArgumentCaptor<CancelAppointmentCommand> cmd = ArgumentCaptor.forClass(CancelAppointmentCommand.class);
        verify(cancelAppointment).execute(cmd.capture());
        assertThat(cmd.getValue().appointmentId()).isEqualTo("a1");
        assertThat(cmd.getValue().authenticatedUserId()).isEqualTo("u1");
        assertThat(cmd.getValue().cancellationReason()).isEqualTo("Cancelado pelo paciente via WhatsApp");
        assertThat(result).isEqualTo("Consulta cancelada com sucesso.");
    }

    @Test
    void cancelAppointment_requiresRegisteredUser() throws Exception {
        assertThat(executor.execute(null, "cancel_appointment", json("{\"appointment_id\":\"a1\"}")))
                .isEqualTo("Usuário não cadastrado.");
        verifyNoInteractions(cancelAppointment);
    }

    @Test
    void useCaseFailuresAreReturnedAsToolOutput() throws Exception {
        when(cancelAppointment.execute(any())).thenThrow(new IllegalStateException("consulta já realizada"));

        assertThat(executor.execute("u1", "cancel_appointment", json("{\"appointment_id\":\"a1\",\"reason\":\"x\"}")))
                .isEqualTo("Erro ao executar ação: consulta já realizada");
    }
}
