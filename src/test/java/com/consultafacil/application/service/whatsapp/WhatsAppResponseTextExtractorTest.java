package com.consultafacil.application.service.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppResponseTextExtractorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WhatsAppResponseTextExtractor extractor = new WhatsAppResponseTextExtractor();

    @Test
    void extractText_returnsFirstTextBlock() throws Exception {
        var response = objectMapper.readTree("""
                {"content":[
                  {"type":"tool_use","id":"t1","name":"x","input":{}},
                  {"type":"text","text":"Consulta agendada!"},
                  {"type":"text","text":"ignorado"}
                ]}""");

        assertThat(extractor.extractText(response)).isEqualTo("Consulta agendada!");
    }

    @Test
    void extractText_fallsBackToApologyWhenThereIsNoText() throws Exception {
        var response = objectMapper.readTree("{\"content\":[{\"type\":\"tool_use\",\"id\":\"t1\"}]}");

        assertThat(extractor.extractText(response)).isEqualTo("Desculpe, não consegui processar sua solicitação.");
    }

    @Test
    void extractText_handlesResponseWithoutContent() throws Exception {
        assertThat(extractor.extractText(objectMapper.readTree("{}")))
                .isEqualTo("Desculpe, não consegui processar sua solicitação.");
    }
}
