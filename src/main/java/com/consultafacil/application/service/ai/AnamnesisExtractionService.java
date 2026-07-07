package com.consultafacil.application.service.ai;

import com.consultafacil.api.dto.ai.ChatMessage;
import com.consultafacil.application.port.in.ai.AnamnesisExtractionUseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.consultafacil.application.service.ai.AiConstants.MODEL_HAIKU;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnamnesisExtractionService implements AnamnesisExtractionUseCase {

    private static final String SYSTEM_PROMPT = """
            Você receberá uma conversa entre assistente e paciente sobre anamnese médica.
            Extraia as informações e retorne APENAS um JSON válido com estes campos (strings, vazio se não mencionado):
            {"chiefComplaint":"","currentMedications":"","allergies":"","medicalHistory":"","familyHistory":"","observations":""}
            Responda APENAS o JSON, sem markdown, sem explicação.
            """;

    private final AnthropicChatRequestBuilder requestBuilder;
    private final AnthropicSyncClient anthropicClient;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, String> execute(List<ChatMessage> messages) {
        try {
            String conversation = messages.stream()
                    .map(m -> (m.role().equals("user") ? "Paciente" : "Assistente") + ": " + m.content())
                    .collect(Collectors.joining("\n"));

            List<ChatMessage> payload = List.of(new ChatMessage("user", conversation));
            String requestBody = requestBuilder.build(MODEL_HAIKU, SYSTEM_PROMPT, payload, 512, false);
            String text = anthropicClient.send(requestBody);

            return objectMapper.readValue(text, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.error("[AnamnesisExtractionService] Error extracting anamnesis: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
}
