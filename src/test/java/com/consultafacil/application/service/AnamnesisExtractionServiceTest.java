package com.consultafacil.application.service;

import com.consultafacil.api.dto.ai.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnamnesisExtractionServiceTest {

    @Mock AnthropicSyncClient anthropicClient;

    AnamnesisExtractionService service;

    @BeforeEach
    void setUp() {
        service = new AnamnesisExtractionService(
                new AnthropicChatRequestBuilder(new ObjectMapper()), anthropicClient, new ObjectMapper());
    }

    @Test
    void extract_validResponse_returnsParsedMap() throws Exception {
        when(anthropicClient.send(org.mockito.ArgumentMatchers.any()))
                .thenReturn("{\"chiefComplaint\":\"Dor de cabeça\",\"allergies\":\"Nenhuma\"}");

        Map<String, String> result = service.extract(List.of(new ChatMessage("user", "Estou com dor de cabeça")));

        assertThat(result).containsEntry("chiefComplaint", "Dor de cabeça");
        assertThat(result).containsEntry("allergies", "Nenhuma");
    }

    @Test
    void extract_clientThrows_returnsEmptyMap() throws Exception {
        when(anthropicClient.send(org.mockito.ArgumentMatchers.any())).thenThrow(new RuntimeException("boom"));

        Map<String, String> result = service.extract(List.of(new ChatMessage("user", "oi")));

        assertThat(result).isEmpty();
    }
}
