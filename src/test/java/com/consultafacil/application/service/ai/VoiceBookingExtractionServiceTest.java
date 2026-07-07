package com.consultafacil.application.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoiceBookingExtractionServiceTest {

    @Mock AnthropicSyncClient anthropicClient;

    VoiceBookingExtractionService service;

    @BeforeEach
    void setUp() {
        service = new VoiceBookingExtractionService(
                new AnthropicChatRequestBuilder(new ObjectMapper()), anthropicClient, new ObjectMapper());
    }

    @Test
    void process_validJsonResponse_returnsParsedDTO() throws Exception {
        when(anthropicClient.send(any())).thenReturn("""
                {"specialty":"Cardiologia","professionalName":null,"date":"2026-08-01",
                 "timePreference":"morning","modality":"IN_PERSON","reason":"Check-up",
                 "confidence":"high","summary":"Consulta de rotina"}
                """);

        var result = service.execute("Quero marcar uma consulta com cardiologista amanhã de manhã");

        assertThat(result.specialty()).isEqualTo("Cardiologia");
        assertThat(result.confidence()).isEqualTo("high");
    }

    @Test
    void process_noJsonInResponse_throwsUnprocessable() throws Exception {
        when(anthropicClient.send(any())).thenReturn("resposta sem json nenhum");

        assertThatThrownBy(() -> service.execute("transcript qualquer"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
