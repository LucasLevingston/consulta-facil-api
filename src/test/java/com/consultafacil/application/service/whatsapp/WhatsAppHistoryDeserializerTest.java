package com.consultafacil.application.service.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppHistoryDeserializerTest {

    private final WhatsAppHistoryDeserializer deserializer = new WhatsAppHistoryDeserializer(new ObjectMapper());

    @Test
    void deserialize_parsesStoredHistory() {
        List<Map<String, Object>> history = deserializer.deserialize(
                "[{\"role\":\"user\",\"content\":\"oi\"},{\"role\":\"assistant\",\"content\":\"olá\"}]");

        assertThat(history).hasSize(2);
        assertThat(history.get(0)).containsEntry("role", "user").containsEntry("content", "oi");
        assertThat(history.get(1)).containsEntry("role", "assistant");
    }

    @Test
    void deserialize_returnsEmptyMutableListForCorruptedJson() {
        List<Map<String, Object>> history = deserializer.deserialize("{not json");

        assertThat(history).isEmpty();
        history.add(Map.of("role", "user", "content", "ainda funciona"));
        assertThat(history).hasSize(1);
    }

    @Test
    void deserialize_returnsEmptyListForNull() {
        assertThat(deserializer.deserialize(null)).isEmpty();
    }
}
