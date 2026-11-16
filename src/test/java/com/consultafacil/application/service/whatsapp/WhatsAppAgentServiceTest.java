package com.consultafacil.application.service.whatsapp;

import com.consultafacil.application.service.ai.AnthropicMessagesClient;
import com.consultafacil.core.config.AnthropicProperties;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.entity.WhatsAppConversation;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import com.consultafacil.domain.port.out.whatsapp.WhatsAppConversationRepositoryPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsAppAgentServiceTest {

    @Mock WhatsAppConversationRepositoryPort conversationRepository;
    @Mock UserRepositoryPort userRepository;
    @Mock AnthropicMessagesClient anthropicClient;
    @Mock WhatsAppToolExecutor toolExecutor;

    final ObjectMapper objectMapper = new ObjectMapper();
    final AnthropicProperties properties = new AnthropicProperties();
    WhatsAppAgentService service;

    @BeforeEach
    void setUp() {
        properties.setApiKey("sk-test");
        service = new WhatsAppAgentService(properties, conversationRepository, userRepository, anthropicClient,
                new WhatsAppToolSchemaBuilder(objectMapper), toolExecutor,
                new WhatsAppHistoryDeserializer(objectMapper), new WhatsAppHistoryTrimmer(),
                new WhatsAppHistorySerializer(objectMapper), new WhatsAppSystemPromptBuilder(),
                new WhatsAppResponseTextExtractor(), objectMapper);
    }

    private JsonNode json(String raw) throws Exception {
        return objectMapper.readTree(raw);
    }

    private WhatsAppConversation savedConversation() {
        ArgumentCaptor<WhatsAppConversation> captor = ArgumentCaptor.forClass(WhatsAppConversation.class);
        verify(conversationRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void processMessage_withoutApiKey_answersUnavailableWithoutCallingClaude() throws Exception {
        properties.setApiKey(" ");

        String reply = service.processMessage("whatsapp:+5511999990000", "oi");

        assertThat(reply).contains("temporariamente indisponível");
        verifyNoInteractions(anthropicClient, conversationRepository, userRepository);
    }

    @Test
    void processMessage_startsConversationForUnknownNumberAndPersistsHistory() throws Exception {
        when(userRepository.findByPhone("+5511999990000")).thenReturn(Optional.empty());
        when(conversationRepository.findByPhoneNumber("+5511999990000")).thenReturn(Optional.empty());
        when(anthropicClient.send(anyString(), anyList(), notNull()))
                .thenReturn(json("{\"stop_reason\":\"end_turn\",\"content\":[{\"type\":\"text\",\"text\":\"Olá! Como posso ajudar?\"}]}"));

        String reply = service.processMessage("whatsapp: +5511999990000 ", "oi");

        assertThat(reply).isEqualTo("Olá! Como posso ajudar?");
        WhatsAppConversation saved = savedConversation();
        assertThat(saved.getPhoneNumber()).isEqualTo("+5511999990000");
        List<Map<String, Object>> history = new WhatsAppHistoryDeserializer(objectMapper).deserialize(saved.getHistoryJson());
        assertThat(history).extracting(m -> m.get("role")).containsExactly("user", "assistant");
        assertThat(history.get(0)).containsEntry("content", "oi");
        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);
        verify(anthropicClient).send(systemPrompt.capture(), anyList(), notNull());
        assertThat(systemPrompt.getValue()).contains("NÃO cadastrado");
        verifyNoInteractions(toolExecutor);
    }

    @Test
    void processMessage_runsRequestedToolAndReturnsFollowUpAnswer() throws Exception {
        when(userRepository.findByPhone("+5511888880000")).thenReturn(Optional.of(User.builder().id("user-7").build()));
        when(conversationRepository.findByPhoneNumber("+5511888880000")).thenReturn(Optional.of(
                WhatsAppConversation.builder().phoneNumber("+5511888880000")
                        .historyJson("[{\"role\":\"user\",\"content\":\"oi\"},{\"role\":\"assistant\",\"content\":\"olá\"}]")
                        .build()));
        when(anthropicClient.send(anyString(), anyList(), notNull())).thenReturn(json("""
                {"stop_reason":"tool_use","content":[
                  {"type":"text","text":"Vou verificar"},
                  {"type":"tool_use","id":"toolu_1","name":"list_appointments","input":{}}
                ]}"""));
        when(toolExecutor.execute(eq("user-7"), eq("list_appointments"), any())).thenReturn("Nenhuma consulta encontrada.");
        when(anthropicClient.send(anyString(), anyList(), isNull()))
                .thenReturn(json("{\"content\":[{\"type\":\"text\",\"text\":\"Você não tem consultas marcadas.\"}]}"));

        String reply = service.processMessage("whatsapp:+5511888880000", "minhas consultas?");

        assertThat(reply).isEqualTo("Você não tem consultas marcadas.");
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Map<String, Object>>> followUpMessages = ArgumentCaptor.forClass(List.class);
        verify(anthropicClient).send(anyString(), followUpMessages.capture(), isNull());
        Map<String, Object> toolResultTurn = followUpMessages.getValue().get(followUpMessages.getValue().size() - 1);
        assertThat(toolResultTurn).containsEntry("role", "user");
        assertThat(toolResultTurn.get("content").toString())
                .contains("tool_result").contains("toolu_1").contains("Nenhuma consulta encontrada.");
        List<Map<String, Object>> history = new WhatsAppHistoryDeserializer(objectMapper)
                .deserialize(savedConversation().getHistoryJson());
        assertThat(history).hasSize(4);
        assertThat(history.get(3)).containsEntry("content", "Você não tem consultas marcadas.");
    }

    @Test
    void processMessage_claudeFailure_returnsApologyAndKeepsStoredHistory() throws Exception {
        when(userRepository.findByPhone("+5511777770000")).thenReturn(Optional.empty());
        when(conversationRepository.findByPhoneNumber("+5511777770000")).thenReturn(Optional.empty());
        when(anthropicClient.send(anyString(), anyList(), notNull())).thenThrow(new RuntimeException("503"));

        String reply = service.processMessage("whatsapp:+5511777770000", "oi");

        assertThat(reply).isEqualTo("Desculpe, ocorreu um erro. Tente novamente em instantes.");
        verify(conversationRepository, never()).save(any());
    }
}
