package com.consultafacil.application.service;

import com.consultafacil.api.dto.ai.ChatMessage;
import com.consultafacil.core.config.AnthropicProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.consultafacil.application.service.AiConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnamnesisChatStreamService {

    private static final String SYSTEM_PROMPT = """
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

    private final AnthropicProperties anthropicProperties;
    private final AnthropicChatRequestBuilder requestBuilder;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public StreamingResponseBody stream(List<ChatMessage> messages) {
        return (OutputStream outputStream) -> {
            try {
                String requestBody = requestBuilder.build(MODEL_SONNET, SYSTEM_PROMPT, messages, 512, true);

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
                                outputStream.write(text.getBytes(StandardCharsets.UTF_8));
                                outputStream.flush();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[AnamnesisChatStreamService] Error streaming anamnesis chat: {}", e.getMessage(), e);
                throw new RuntimeException("Erro ao processar resposta de IA", e);
            }
        };
    }
}
