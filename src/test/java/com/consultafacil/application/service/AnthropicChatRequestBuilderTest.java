package com.consultafacil.application.service;

import com.consultafacil.api.dto.ai.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnthropicChatRequestBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AnthropicChatRequestBuilder builder = new AnthropicChatRequestBuilder(objectMapper);

    @Test
    void build_includesModelMaxTokensAndStream() throws Exception {
        String json = builder.build("model-x", "system prompt",
                List.of(new ChatMessage("user", "oi")), 256, true);

        JsonNode node = objectMapper.readTree(json);
        assertThat(node.path("model").asText()).isEqualTo("model-x");
        assertThat(node.path("max_tokens").asInt()).isEqualTo(256);
        assertThat(node.path("stream").asBoolean()).isTrue();
        assertThat(node.path("system").asText()).isEqualTo("system prompt");
        assertThat(node.path("messages").get(0).path("role").asText()).isEqualTo("user");
        assertThat(node.path("messages").get(0).path("content").asText()).isEqualTo("oi");
    }

    @Test
    void build_blankSystemPrompt_omitsSystemField() throws Exception {
        String json = builder.build("model-x", null, List.of(new ChatMessage("user", "oi")), 256, false);

        JsonNode node = objectMapper.readTree(json);
        assertThat(node.has("system")).isFalse();
    }
}
