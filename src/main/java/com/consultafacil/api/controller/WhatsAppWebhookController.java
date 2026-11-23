package com.consultafacil.api.controller;

import com.consultafacil.application.port.in.whatsapp.WhatsAppWebhookUseCase;
import com.consultafacil.core.security.TwilioWebhookValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/webhook/whatsapp")
@RequiredArgsConstructor
@Tag(name = "WhatsApp Webhook", description = "Twilio WhatsApp incoming message webhook")
public class WhatsAppWebhookController {

    private final WhatsAppWebhookUseCase whatsAppWebhook;
    private final TwilioWebhookValidator twilioWebhookValidator;

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Receive WhatsApp message from Twilio")
    public String receiveMessage(
            @RequestHeader(value = "X-Twilio-Signature", required = false) String twilioSignature,
            @RequestParam Map<String, String> allParams,
            @RequestParam(value = "From", defaultValue = "") String from,
            @RequestParam(value = "Body", defaultValue = "") String body,
            HttpServletRequest request) {

        twilioWebhookValidator.validate(resolveRequestUrl(request), allParams, twilioSignature);

        log.info("[WhatsAppWebhook] Message from {} — body length {}", from, body.length());
        String reply = whatsAppWebhook.processMessage(from, body);
        return twiml(reply);
    }

    /**
     * Reconstructs the full public URL that Twilio used to reach this endpoint.
     * Twilio signs using the exact URL it called, including scheme and host.
     * Behind a reverse proxy the {@code X-Forwarded-Proto} / {@code X-Forwarded-Host}
     * headers carry the original scheme and host.
     */
    private String resolveRequestUrl(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost  = request.getHeader("X-Forwarded-Host");

        String scheme = (forwardedProto != null && !forwardedProto.isBlank())
                ? forwardedProto
                : request.getScheme();

        String host = (forwardedHost != null && !forwardedHost.isBlank())
                ? forwardedHost
                : request.getServerName() + (request.getServerPort() != 80 && request.getServerPort() != 443
                    ? ":" + request.getServerPort()
                    : "");

        return scheme + "://" + host + request.getRequestURI();
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
