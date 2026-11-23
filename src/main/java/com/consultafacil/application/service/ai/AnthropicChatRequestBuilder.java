package com.consultafacil.application.service.ai;

import com.consultafacil.api.dto.ai.ChatMessage;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnthropicChatRequestBuilder {

    private final ObjectMapper objectMapper;

    public String build(String model, String systemPrompt, List<ChatMessage> messages,
                         int maxTokens, boolean stream) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("stream", stream);

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            body.put("system", systemPrompt);
        }

        ArrayNode messagesNode = objectMapper.createArrayNode();
        for (ChatMessage msg : messages) {
            ObjectNode msgNode = objectMapper.createObjectNode();
            msgNode.put("role", msg.role());
            msgNode.put("content", msg.content());
            messagesNode.add(msgNode);
        }
        body.set("messages", messagesNode);

        return objectMapper.writeValueAsString(body);
    }
}
