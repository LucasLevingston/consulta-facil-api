package com.consultafacil.application.service.whatsapp;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppHistoryTrimmerTest {

    private final WhatsAppHistoryTrimmer trimmer = new WhatsAppHistoryTrimmer();

    private List<Map<String, Object>> messages(int count) {
        List<Map<String, Object>> list = new ArrayList<>();
        IntStream.range(0, count).forEach(i -> list.add(Map.of("role", "user", "content", "m" + i)));
        return list;
    }

    @Test
    void trim_keepsHistoryUpToTwentyMessagesUntouched() {
        List<Map<String, Object>> history = messages(20);

        assertThat(trimmer.trim(history)).isSameAs(history);
    }

    @Test
    void trim_keepsOnlyTheMostRecentTwentyMessages() {
        List<Map<String, Object>> trimmed = trimmer.trim(messages(25));

        assertThat(trimmed).hasSize(20);
        assertThat(trimmed.get(0)).containsEntry("content", "m5");
        assertThat(trimmed.get(19)).containsEntry("content", "m24");
    }

    @Test
    void trim_returnsAnIndependentMutableList() {
        List<Map<String, Object>> original = messages(21);

        List<Map<String, Object>> trimmed = trimmer.trim(original);
        trimmed.add(Map.of("role", "assistant", "content", "nova"));

        assertThat(original).hasSize(21);
    }
}
