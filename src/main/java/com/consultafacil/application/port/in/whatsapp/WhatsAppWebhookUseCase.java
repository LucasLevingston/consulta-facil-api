package com.consultafacil.application.port.in.whatsapp;

public interface WhatsAppWebhookUseCase {

    String processMessage(String from, String body);
}
