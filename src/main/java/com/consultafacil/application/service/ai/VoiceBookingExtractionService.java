package com.consultafacil.application.service.ai;

import com.consultafacil.api.dto.ai.ChatMessage;
import com.consultafacil.api.dto.ai.VoiceBookingResponseDTO;
import com.consultafacil.application.port.in.ai.VoiceBookingExtractionUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.consultafacil.application.service.ai.AiConstants.MODEL_HAIKU;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceBookingExtractionService implements VoiceBookingExtractionUseCase {

    private final AnthropicChatRequestBuilder requestBuilder;
    private final AnthropicSyncClient anthropicClient;
    private final ObjectMapper objectMapper;

    @Override
    public VoiceBookingResponseDTO execute(String transcript) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String userMessage = buildPrompt(today, transcript);

        try {
            List<ChatMessage> payload = List.of(new ChatMessage("user", userMessage));
            String requestBody = requestBuilder.build(MODEL_HAIKU, null, payload, 512, false);
            String text = anthropicClient.send(requestBody);

            Pattern pattern = Pattern.compile("\\{[\\s\\S]*\\}");
            Matcher matcher = pattern.matcher(text);
            if (!matcher.find()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Falha ao processar");
            }

            return objectMapper.readValue(matcher.group(), VoiceBookingResponseDTO.class);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("[VoiceBookingExtractionService] Error processing voice booking: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Falha ao processar");
        }
    }

    private String buildPrompt(String today, String transcript) {
        return String.format("""
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
    }
}
