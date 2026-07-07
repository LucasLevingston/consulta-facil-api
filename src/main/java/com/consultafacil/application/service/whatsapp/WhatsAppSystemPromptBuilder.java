package com.consultafacil.application.service.whatsapp;

import org.springframework.stereotype.Component;

@Component
public class WhatsAppSystemPromptBuilder {

    public String buildSystemPrompt(String userId, String today) {
        String userCtx = userId != null
                ? "Paciente autenticado (userId: " + userId + ")."
                : "Paciente NÃO cadastrado — oriente-o a se registrar no app.";
        return String.format("""
                Você é o assistente de agendamento do Consulta Fácil. Hoje: %s. %s
                Pode buscar profissionais, agendar, listar e cancelar consultas.
                Seja conciso e amigável em português brasileiro.
                Confirme dados antes de agendar. Use as ferramentas disponíveis.
                """, today, userCtx);
    }
}
