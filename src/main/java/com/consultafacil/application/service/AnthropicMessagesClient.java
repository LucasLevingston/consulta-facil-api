package com.consultafacil.application.service;

import com.consultafacil.core.config.AnthropicProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AnthropicMessagesClient {

    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";

    private final AnthropicProperties anthropicProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public JsonNode send(String systemPrompt, List<Map<String, Object>> messages, ArrayNode tools) throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", anthropicProperties.getModel());
        requestBody.put("max_tokens", 1024);
        requestBody.put("system", systemPrompt);
        requestBody.set("messages", objectMapper.valueToTree(messages));
        if (tools != null) {
            requestBody.set("tools", tools);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicProperties.getApiKey());
        headers.set("anthropic-version", "2023-06-01");

        String responseJson = restTemplate.postForObject(
                ANTHROPIC_URL, new HttpEntity<>(requestBody.toString(), headers), String.class);
        return objectMapper.readTree(responseJson);
    }
}
