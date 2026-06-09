package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.CancelAppointmentDTO;
import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.core.config.AnthropicProperties;
import com.consultafacil.domain.entity.WhatsAppConversation;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.consultafacil.domain.port.out.WhatsAppConversationRepositoryPort;
import com.consultafacil.application.port.in.WhatsAppWebhookUseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppAgentService implements WhatsAppWebhookUseCase {

    private final AnthropicProperties anthropicProperties;
    private final WhatsAppConversationRepositoryPort conversationRepository;
    private final UserRepositoryPort userRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final AppointmentService appointmentService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final int MAX_HISTORY = 20;
    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";

    @Transactional
    public String processMessage(String fromWhatsApp, String messageBody) {
        String phone = fromWhatsApp.replace("whatsapp:", "").trim();

        if (anthropicProperties.getApiKey() == null || anthropicProperties.getApiKey().isBlank()) {
            log.warn("[WhatsAppAgent] ANTHROPIC_API_KEY não configurada");
            return "Desculpe, o assistente de IA está temporariamente indisponível.";
        }

        var userOpt = userRepository.findByPhone(phone);
        String userId = userOpt.map(u -> u.getId()).orElse(null);

        WhatsAppConversation conversation = conversationRepository.findByPhoneNumber(phone)
                .orElseGet(() -> WhatsAppConversation.builder().phoneNumber(phone).build());

        List<Map<String, Object>> history = deserializeHistory(conversation.getHistoryJson());
        history.add(Map.of("role", "user", "content", messageBody));

        try {
            String reply = callClaude(userId, history);
            history.add(Map.of("role", "assistant", "content", reply));
            if (history.size() > MAX_HISTORY) {
                history = new ArrayList<>(history.subList(history.size() - MAX_HISTORY, history.size()));
            }
            conversation.setHistoryJson(objectMapper.writeValueAsString(history));
            conversationRepository.save(conversation);
            return reply;
        } catch (Exception e) {
            log.error("[WhatsAppAgent] Erro ao processar mensagem: {}", e.getMessage());
            return "Desculpe, ocorreu um erro. Tente novamente em instantes.";
        }
    }

    private String callClaude(String userId, List<Map<String, Object>> history) throws Exception {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", anthropicProperties.getModel());
        requestBody.put("max_tokens", 1024);
        requestBody.put("system", buildSystemPrompt(userId, today));
        requestBody.set("messages", objectMapper.valueToTree(history));
        requestBody.set("tools", buildTools());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicProperties.getApiKey());
        headers.set("anthropic-version", "2023-06-01");

        String responseJson = restTemplate.postForObject(
                ANTHROPIC_URL, new HttpEntity<>(requestBody.toString(), headers), String.class);

        JsonNode response = objectMapper.readTree(responseJson);
        String stopReason = response.path("stop_reason").asText();

        if ("tool_use".equals(stopReason)) {
            return handleToolUse(userId, response, history, today);
        }

        return extractText(response);
    }

    private String handleToolUse(String userId, JsonNode response,
                                 List<Map<String, Object>> history, String today) throws Exception {
        JsonNode assistantContent = response.path("content");
        List<Map<String, Object>> updatedHistory = new ArrayList<>(history);
        updatedHistory.add(Map.of("role", "assistant", "content",
                objectMapper.convertValue(assistantContent, Object.class)));

        List<Map<String, Object>> toolResults = new ArrayList<>();
        for (JsonNode block : assistantContent) {
            if ("tool_use".equals(block.path("type").asText())) {
                String toolName = block.path("name").asText();
                String toolId = block.path("id").asText();
                JsonNode input = block.path("input");
                String result = executeTool(userId, toolName, input);
                toolResults.add(Map.of(
                        "type", "tool_result",
                        "tool_use_id", toolId,
                        "content", result));
            }
        }

        updatedHistory.add(Map.of("role", "user", "content", toolResults));

        ObjectNode followUp = objectMapper.createObjectNode();
        followUp.put("model", anthropicProperties.getModel());
        followUp.put("max_tokens", 1024);
        followUp.put("system", buildSystemPrompt(userId, today));
        followUp.set("messages", objectMapper.valueToTree(updatedHistory));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicProperties.getApiKey());
        headers.set("anthropic-version", "2023-06-01");

        String responseJson = restTemplate.postForObject(
                ANTHROPIC_URL, new HttpEntity<>(followUp.toString(), headers), String.class);

        return extractText(objectMapper.readTree(responseJson));
    }

    private String executeTool(String userId, String toolName, JsonNode input) {
        try {
            return switch (toolName) {
                case "search_professionals" -> {
                    String specialty = input.path("specialty").asText("");
                    var page = professionalProfileRepository.findActiveWithFilters(
                            "", specialty, "", PageRequest.of(0, 5));
                    if (page.isEmpty()) yield "Nenhum profissional encontrado para a especialidade informada.";
                    var sb = new StringBuilder("Profissionais encontrados:\n");
                    page.getContent().forEach(p -> sb
                            .append("- ID: ").append(p.getId())
                            .append(", Nome: ").append(p.getUser().getName())
                            .append(", Especialidade: ").append(p.getSpecialty()).append("\n"));
                    yield sb.toString();
                }
                case "list_appointments" -> {
                    if (userId == null) yield "Usuário não cadastrado. Acesse o app para se registrar.";
                    var patientOpt = patientProfileRepository.findByUserId(userId);
                    if (patientOpt.isEmpty()) yield "Perfil de paciente não encontrado.";
                    var appts = appointmentRepository.findByPatientId(
                            patientOpt.get().getId(), PageRequest.of(0, 5));
                    if (appts.isEmpty()) yield "Nenhuma consulta encontrada.";
                    var sb = new StringBuilder("Suas consultas:\n");
                    appts.getContent().stream()
                            .filter(a -> a.getStatus() != AppointmentStatus.CANCELED
                                    && a.getStatus() != AppointmentStatus.COMPLETED)
                            .forEach(a -> sb
                                    .append("- ID: ").append(a.getId())
                                    .append(", Médico: ").append(a.getProfessional().getUser().getName())
                                    .append(", Data: ").append(a.getScheduledAt()
                                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                                    .append(", Status: ").append(a.getStatus()).append("\n"));
                    yield sb.toString();
                }
                case "book_appointment" -> {
                    if (userId == null) yield "Usuário não cadastrado. Acesse o app para se registrar.";
                    String professionalId = input.path("professional_id").asText();
                    String dateTimeStr = input.path("date_time").asText();
                    String reason = input.path("reason").asText(null);
                    LocalDateTime scheduledAt = LocalDateTime.parse(dateTimeStr,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                    var dto = CreateAppointmentDTO.builder()
                            .professionalId(professionalId)
                            .scheduledAt(scheduledAt)
                            .reason(reason)
                            .modality(AppointmentModality.IN_PERSON)
                            .build();
                    var appt = appointmentService.scheduleAppointment(userId, dto);
                    yield "Consulta agendada! ID: " + appt.getId() + ", Data: "
                            + appt.getScheduledAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                }
                case "cancel_appointment" -> {
                    if (userId == null) yield "Usuário não cadastrado.";
                    String appointmentId = input.path("appointment_id").asText();
                    String reason = input.path("reason").asText("Cancelado pelo paciente via WhatsApp");
                    appointmentService.cancelAppointment(appointmentId, userId,
                            new CancelAppointmentDTO(reason));
                    yield "Consulta cancelada com sucesso.";
                }
                default -> "Ferramenta desconhecida: " + toolName;
            };
        } catch (Exception e) {
            log.error("[WhatsAppAgent] Erro ao executar {}: {}", toolName, e.getMessage());
            return "Erro ao executar ação: " + e.getMessage();
        }
    }

    private String buildSystemPrompt(String userId, String today) {
        String userCtx = userId != null
                ? "Paciente autenticado (userId: " + userId + ")."
                : "Paciente NÃO cadastrado — oriente-o a se registrar no app.";
        return String.format("""
                Você é o assistente de agendamento do Consulta Fácil. Hoje: %s. %s
                Pode buscar profissionais, agendar, listar e cancelar consultas.
                Seja conciso e amigável em português brasileiro.
                Confirme dados antes de agendar. Use as ferramentas disponíveis.
                """, today, userCtx);
    }

    private ArrayNode buildTools() {
        ArrayNode tools = objectMapper.createArrayNode();

        ObjectNode searchSchema = objectMapper.createObjectNode();
        searchSchema.put("type", "object");
        ObjectNode searchProps = objectMapper.createObjectNode();
        ObjectNode specialtyProp = objectMapper.createObjectNode();
        specialtyProp.put("type", "string");
        specialtyProp.put("description", "Especialidade médica");
        searchProps.set("specialty", specialtyProp);
        searchSchema.set("properties", searchProps);
        searchSchema.set("required", objectMapper.createArrayNode());
        ObjectNode searchTool = objectMapper.createObjectNode();
        searchTool.put("name", "search_professionals");
        searchTool.put("description", "Busca profissionais por especialidade");
        searchTool.set("input_schema", searchSchema);
        tools.add(searchTool);

        ObjectNode listSchema = objectMapper.createObjectNode();
        listSchema.put("type", "object");
        listSchema.set("properties", objectMapper.createObjectNode());
        ObjectNode listTool = objectMapper.createObjectNode();
        listTool.put("name", "list_appointments");
        listTool.put("description", "Lista consultas do paciente");
        listTool.set("input_schema", listSchema);
        tools.add(listTool);

        ObjectNode bookSchema = objectMapper.createObjectNode();
        bookSchema.put("type", "object");
        ObjectNode bookProps = objectMapper.createObjectNode();
        ObjectNode profIdProp = objectMapper.createObjectNode();
        profIdProp.put("type", "string");
        profIdProp.put("description", "ID do profissional");
        bookProps.set("professional_id", profIdProp);
        ObjectNode dtProp = objectMapper.createObjectNode();
        dtProp.put("type", "string");
        dtProp.put("description", "Data e hora: yyyy-MM-ddTHH:mm");
        bookProps.set("date_time", dtProp);
        ObjectNode reasonProp = objectMapper.createObjectNode();
        reasonProp.put("type", "string");
        reasonProp.put("description", "Motivo da consulta");
        bookProps.set("reason", reasonProp);
        bookSchema.set("properties", bookProps);
        bookSchema.set("required", objectMapper.createArrayNode().add("professional_id").add("date_time"));
        ObjectNode bookTool = objectMapper.createObjectNode();
        bookTool.put("name", "book_appointment");
        bookTool.put("description", "Agenda uma consulta para o paciente");
        bookTool.set("input_schema", bookSchema);
        tools.add(bookTool);

        ObjectNode cancelSchema = objectMapper.createObjectNode();
        cancelSchema.put("type", "object");
        ObjectNode cancelProps = objectMapper.createObjectNode();
        ObjectNode apptIdProp = objectMapper.createObjectNode();
        apptIdProp.put("type", "string");
        apptIdProp.put("description", "ID da consulta");
        cancelProps.set("appointment_id", apptIdProp);
        ObjectNode cancelReasonProp = objectMapper.createObjectNode();
        cancelReasonProp.put("type", "string");
        cancelReasonProp.put("description", "Motivo do cancelamento");
        cancelProps.set("reason", cancelReasonProp);
        cancelSchema.set("properties", cancelProps);
        cancelSchema.set("required", objectMapper.createArrayNode().add("appointment_id"));
        ObjectNode cancelTool = objectMapper.createObjectNode();
        cancelTool.put("name", "cancel_appointment");
        cancelTool.put("description", "Cancela uma consulta do paciente");
        cancelTool.set("input_schema", cancelSchema);
        tools.add(cancelTool);

        return tools;
    }

    private String extractText(JsonNode response) {
        for (JsonNode block : response.path("content")) {
            if ("text".equals(block.path("type").asText())) {
                return block.path("text").asText();
            }
        }
        return "Desculpe, não consegui processar sua solicitação.";
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> deserializeHistory(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
