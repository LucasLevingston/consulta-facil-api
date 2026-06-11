package com.consultafacil.application.service;

import com.consultafacil.api.dto.ai.ChatMessage;
import com.consultafacil.api.dto.ai.VoiceBookingResponseDTO;
import com.consultafacil.core.config.AnthropicProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final String MODEL_SONNET = "claude-sonnet-4-6";
    private static final String MODEL_HAIKU = "claude-haiku-4-5-20251001";

    private static final String ANAMNESIS_SYSTEM_PROMPT = """
            Você é um assistente médico empático da plataforma Consulta Fácil.
            Seu objetivo é ajudar o paciente a preencher os campos de anamnese antes de uma consulta.
            Converse em português brasileiro de forma acolhedora.
            Pergunte UMA coisa por vez, na seguinte ordem:
            1. Qual é sua queixa principal hoje? (motivo da consulta)
            2. Usa algum medicamento regularmente? Se sim, quais?
            3. Tem alguma alergia conhecida (medicamentos, alimentos, outras)?
            4. Tem algum histórico médico relevante (doenças, cirurgias, internações)?
            5. Há doenças na família que considera importante mencionar?
            6. Alguma observação adicional para o profissional?
            Após coletar todas as informações, informe o paciente que pode clicar em "Salvar na anamnese".
            Não faça diagnósticos. Não substitua consulta médica.
            """;

    private static final String EXTRACT_SYSTEM_PROMPT = """
            Você receberá uma conversa entre assistente e paciente sobre anamnese médica.
            Extraia as informações e retorne APENAS um JSON válido com estes campos (strings, vazio se não mencionado):
            {"chiefComplaint":"","currentMedications":"","allergies":"","medicalHistory":"","familyHistory":"","observations":""}
            Responda APENAS o JSON, sem markdown, sem explicação.
            """;

    private final AnthropicProperties anthropicProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public StreamingResponseBody streamAnamnesisChat(List<ChatMessage> messages) {
        return (OutputStream outputStream) -> {
            try {
                String requestBody = buildRequestBody(MODEL_SONNET, ANAMNESIS_SYSTEM_PROMPT, messages, 512, true);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ANTHROPIC_URL))
                        .header("x-api-key", anthropicProperties.getApiKey())
                        .header("anthropic-version", ANTHROPIC_VERSION)
                        .header("content-type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<java.io.InputStream> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofInputStream());

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("data: ")) {
                            continue;
                        }
                        String data = line.substring(6).trim();
                        if (data.isEmpty()) {
                            continue;
                        }
                        JsonNode event = objectMapper.readTree(data);
                        String type = event.path("type").asText();
                        if ("message_stop".equals(type)) {
                            break;
                        }
                        if ("content_block_delta".equals(type)) {
                            JsonNode delta = event.path("delta");
                            if ("text_delta".equals(delta.path("type").asText())) {
                                String text = delta.path("text").asText();
                                outputStream.write(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                                outputStream.flush();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[AiService] Error streaming anamnesis chat: {}", e.getMessage(), e);
                throw new RuntimeException("Erro ao processar resposta de IA", e);
            }
        };
    }

    public Map<String, String> extractAnamnesis(List<ChatMessage> messages) {
        try {
            String conversation = messages.stream()
                    .map(m -> (m.role().equals("user") ? "Paciente" : "Assistente") + ": " + m.content())
                    .collect(Collectors.joining("\n"));

            List<ChatMessage> payload = List.of(new ChatMessage("user", conversation));
            String requestBody = buildRequestBody(MODEL_HAIKU, EXTRACT_SYSTEM_PROMPT, payload, 512, false);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ANTHROPIC_URL))
                    .header("x-api-key", anthropicProperties.getApiKey())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            String text = root.path("content").get(0).path("text").asText();

            return objectMapper.readValue(text, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.error("[AiService] Error extracting anamnesis: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    public VoiceBookingResponseDTO processVoiceBooking(String transcript) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String userMessage = String.format("""
                Você é um assistente de agendamento médico. Hoje é %s.

                O paciente disse: "%s"

                Extraia as informações de agendamento e responda APENAS com JSON válido neste formato exato:
                {
                  "specialty": "especialidade médica ou null",
                  "professionalName": "nome do médico mencionado ou null",
                  "date": "data no formato YYYY-MM-DD ou null (converta 'amanhã', 'próxima semana', etc.)",
                  "timePreference": "morning|afternoon|evening|any ou null (manhã=morning, tarde=afternoon, noite=evening)",
                  "modality": "IN_PERSON|ONLINE ou null",
                  "reason": "motivo da consulta em uma frase ou null",
                  "confidence": "high|medium|low",
                  "summary": "resumo da solicitação em português em uma frase"
                }

                Responda APENAS com o JSON, sem explicações.
                """, today, transcript);

        try {
            List<ChatMessage> payload = List.of(new ChatMessage("user", userMessage));
            String requestBody = buildRequestBody(MODEL_HAIKU, null, payload, 512, false);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ANTHROPIC_URL))
                    .header("x-api-key", anthropicProperties.getApiKey())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            String text = root.path("content").get(0).path("text").asText();

            Pattern pattern = Pattern.compile("\\{[\\s\\S]*\\}");
            Matcher matcher = pattern.matcher(text);
            if (!matcher.find()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Falha ao processar");
            }

            return objectMapper.readValue(matcher.group(), VoiceBookingResponseDTO.class);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AiService] Error processing voice booking: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Falha ao processar");
        }
    }

    private String buildRequestBody(String model, String systemPrompt,
                                    List<ChatMessage> messages, int maxTokens, boolean stream) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("stream", stream);

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            body.put("system", systemPrompt);
        }

        ArrayNode messagesNode = objectMapper.createArrayNode();
        for (ChatMessage msg : messages) {
            ObjectNode msgNode = objectMapper.createObjectNode();
            msgNode.put("role", msg.role());
            msgNode.put("content", msg.content());
            messagesNode.add(msgNode);
        }
        body.set("messages", messagesNode);

        return objectMapper.writeValueAsString(body);
    }
}
