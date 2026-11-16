package com.consultafacil.application.service.whatsapp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppSystemPromptBuilderTest {

    private final WhatsAppSystemPromptBuilder builder = new WhatsAppSystemPromptBuilder();

    @Test
    void buildSystemPrompt_includesDateAndAuthenticatedUser() {
        String prompt = builder.buildSystemPrompt("user-42", "16/11/2026 10:00");

        assertThat(prompt)
                .contains("Hoje: 16/11/2026 10:00")
                .contains("Paciente autenticado (userId: user-42)")
                .doesNotContain("NÃO cadastrado");
    }

    @Test
    void buildSystemPrompt_tellsAnonymousSenderToRegister() {
        String prompt = builder.buildSystemPrompt(null, "16/11/2026 10:00");

        assertThat(prompt)
                .contains("Paciente NÃO cadastrado")
                .doesNotContain("userId:");
    }
}
