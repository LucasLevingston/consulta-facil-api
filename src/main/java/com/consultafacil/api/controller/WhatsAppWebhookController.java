package com.consultafacil.api.controller;

import com.consultafacil.application.port.in.whatsapp.WhatsAppWebhookUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhook/whatsapp")
@RequiredArgsConstructor
@Tag(name = "WhatsApp Webhook", description = "Twilio WhatsApp incoming message webhook")
public class WhatsAppWebhookController {

    private final WhatsAppWebhookUseCase whatsAppWebhook;

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Receive WhatsApp message from Twilio")
    public String receiveMessage(
            @RequestParam(value = "From", defaultValue = "") String from,
            @RequestParam(value = "Body", defaultValue = "") String body) {

        log.info("[WhatsAppWebhook] Message from {} — body length {}", from, body.length());
        String reply = whatsAppWebhook.processMessage(from, body);
        return twiml(reply);
    }

    private String twiml(String message) {
        String escaped = message
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Message>" + escaped + "</Message></Response>";
    }
}
