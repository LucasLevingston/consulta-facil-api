package com.consultafacil.application.service.whatsapp;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppHistorySerializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serialize_roundTripsThroughTheDeserializer() throws Exception {
        List<Map<String, Object>> history = List.of(
                Map.of("role", "user", "content", "quero marcar consulta"),
                Map.of("role", "assistant", "content", "claro!"));

        String json = new WhatsAppHistorySerializer(objectMapper).serialize(history);

        assertThat(new WhatsAppHistoryDeserializer(objectMapper).deserialize(json)).isEqualTo(history);
    }

    @Test
    void serialize_emptyHistoryProducesEmptyJsonArray() throws Exception {
        assertThat(new WhatsAppHistorySerializer(objectMapper).serialize(List.of())).isEqualTo("[]");
    }
}
