package com.consultafacil.application.service;

import com.consultafacil.application.port.in.WhatsAppWebhookUseCase;
import com.consultafacil.core.config.AnthropicProperties;
import com.consultafacil.domain.entity.WhatsAppConversation;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.consultafacil.domain.port.out.WhatsAppConversationRepositoryPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AnthropicMessagesClient anthropicClient;
    private final WhatsAppToolSchemaBuilder toolSchemaBuilder;
    private final WhatsAppToolExecutor toolExecutor;
    private final WhatsAppHistoryManager historyManager;
    private final WhatsAppPromptFormatter promptFormatter;
    private final ObjectMapper objectMapper;

    @Transactional
    public String processMessage(String fromWhatsApp, String messageBody) {
        String phone = fromWhatsApp.replace("whatsapp:", "").trim();
        if (anthropicProperties.getApiKey() == null || anthropicProperties.getApiKey().isBlank()) {
            log.warn("[WhatsAppAgent] ANTHROPIC_API_KEY não configurada");
            return "Desculpe, o assistente de IA está temporariamente indisponível.";
        }

        String userId = userRepository.findByPhone(phone).map(u -> u.getId()).orElse(null);
        WhatsAppConversation conversation = conversationRepository.findByPhoneNumber(phone)
                .orElseGet(() -> WhatsAppConversation.builder().phoneNumber(phone).build());

        List<Map<String, Object>> history = historyManager.deserialize(conversation.getHistoryJson());
        history.add(Map.of("role", "user", "content", messageBody));

        try {
            String reply = callClaude(userId, history);
            history.add(Map.of("role", "assistant", "content", reply));
            history = historyManager.trim(history);
            conversation.setHistoryJson(historyManager.serialize(history));
            conversationRepository.save(conversation);
            return reply;
        } catch (Exception e) {
            log.error("[WhatsAppAgent] Erro ao processar mensagem: {}", e.getMessage());
            return "Desculpe, ocorreu um erro. Tente novamente em instantes.";
        }
    }

    private String callClaude(String userId, List<Map<String, Object>> history) throws Exception {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        JsonNode response = anthropicClient.send(
                promptFormatter.buildSystemPrompt(userId, today), history, toolSchemaBuilder.build());
        if ("tool_use".equals(response.path("stop_reason").asText())) {
            return handleToolUse(userId, response, history, today);
        }
        return promptFormatter.extractText(response);
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
                String result = toolExecutor.execute(userId, block.path("name").asText(), block.path("input"));
                toolResults.add(Map.of(
                        "type", "tool_result",
                        "tool_use_id", block.path("id").asText(),
                        "content", result));
            }
        }
        updatedHistory.add(Map.of("role", "user", "content", toolResults));

        JsonNode followUp = anthropicClient.send(promptFormatter.buildSystemPrompt(userId, today), updatedHistory, null);
        return promptFormatter.extractText(followUp);
    }
}
