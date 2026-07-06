package com.consultafacil.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppResponseTextExtractor {

    public String extractText(JsonNode response) {
        for (JsonNode block : response.path("content")) {
            if ("text".equals(block.path("type").asText())) {
                return block.path("text").asText();
            }
        }
        return "Desculpe, não consegui processar sua solicitação.";
    }
}
