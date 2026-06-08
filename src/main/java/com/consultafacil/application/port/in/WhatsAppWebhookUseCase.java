package com.consultafacil.application.port.in;

public interface WhatsAppWebhookUseCase {

    String processMessage(String from, String body);
}
