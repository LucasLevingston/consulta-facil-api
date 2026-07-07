package com.consultafacil.application.service.whatsapp;

import com.consultafacil.application.port.in.CancelAppointmentUseCase;
import com.consultafacil.application.port.in.ScheduleAppointmentUseCase;
import com.consultafacil.application.port.in.command.CancelAppointmentCommand;
import com.consultafacil.application.port.in.command.ScheduleAppointmentCommand;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhatsAppToolExecutor {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final ScheduleAppointmentUseCase scheduleAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;

    public String execute(String userId, String toolName, JsonNode input) {
        try {
            return switch (toolName) {
                case "search_professionals" -> searchProfessionals(input);
                case "list_appointments" -> listAppointments(userId);
                case "book_appointment" -> bookAppointment(userId, input);
                case "cancel_appointment" -> cancelAppointment(userId, input);
                default -> "Ferramenta desconhecida: " + toolName;
            };
        } catch (Exception e) {
            log.error("[WhatsAppAgent] Erro ao executar {}: {}", toolName, e.getMessage());
            return "Erro ao executar ação: " + e.getMessage();
        }
    }

    private String searchProfessionals(JsonNode input) {
        String specialty = input.path("specialty").asText("");
        var page = professionalProfileRepository.findActiveWithFilters("", specialty, "", PageRequest.of(0, 5));
        if (page.isEmpty()) return "Nenhum profissional encontrado para a especialidade informada.";
        var sb = new StringBuilder("Profissionais encontrados:\n");
        page.getContent().forEach(p -> sb
                .append("- ID: ").append(p.getId())
                .append(", Nome: ").append(p.getUser().getName())
                .append(", Especialidade: ").append(p.getSpecialty() != null ? p.getSpecialty().name() : "").append("\n"));
        return sb.toString();
    }

    private String listAppointments(String userId) {
        if (userId == null) return "Usuário não cadastrado. Acesse o app para se registrar.";
        var patientOpt = patientProfileRepository.findByUserId(userId);
        if (patientOpt.isEmpty()) return "Perfil de paciente não encontrado.";
        var appts = appointmentRepository.findByPatientId(patientOpt.get().getId(), PageRequest.of(0, 5));
        if (appts.isEmpty()) return "Nenhuma consulta encontrada.";
        var sb = new StringBuilder("Suas consultas:\n");
        appts.getContent().stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELED && a.getStatus() != AppointmentStatus.COMPLETED)
                .forEach(a -> sb
                        .append("- ID: ").append(a.getId())
                        .append(", Médico: ").append(a.getProfessional().getUser().getName())
                        .append(", Data: ").append(a.getScheduledAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                        .append(", Status: ").append(a.getStatus()).append("\n"));
        return sb.toString();
    }

    private String bookAppointment(String userId, JsonNode input) {
        if (userId == null) return "Usuário não cadastrado. Acesse o app para se registrar.";
        LocalDateTime scheduledAt = LocalDateTime.parse(input.path("date_time").asText(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        var appt = scheduleAppointmentUseCase.execute(new ScheduleAppointmentCommand(
                userId, input.path("professional_id").asText(), scheduledAt,
                input.path("reason").asText(null), null, AppointmentModality.IN_PERSON, null, null));
        return "Consulta agendada! ID: " + appt.getId() + ", Data: "
                + appt.getScheduledAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String cancelAppointment(String userId, JsonNode input) {
        if (userId == null) return "Usuário não cadastrado.";
        cancelAppointmentUseCase.execute(new CancelAppointmentCommand(
                input.path("appointment_id").asText(), userId,
                input.path("reason").asText("Cancelado pelo paciente via WhatsApp")));
        return "Consulta cancelada com sucesso.";
    }
}
