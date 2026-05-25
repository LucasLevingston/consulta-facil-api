package com.example.consulta.application.service;

import com.example.consulta.core.config.TwilioProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private final TwilioProperties twilioProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(String toPhone, String body) {
        String accountSid = twilioProperties.getAccountSid();
        String authToken = twilioProperties.getAuthToken();

        if (accountSid == null || accountSid.isBlank() || authToken == null || authToken.isBlank()) {
            log.info("[WhatsApp] Credenciais Twilio não configuradas. Mensagem não enviada para {}. Conteúdo: {}", toPhone, body);
            return;
        }

        if (toPhone == null || toPhone.isBlank()) {
            log.debug("[WhatsApp] Destinatário sem telefone cadastrado. Mensagem ignorada.");
            return;
        }

        String normalizedPhone = toPhone.startsWith("+") ? toPhone : "+" + toPhone;
        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

        String credentials = Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + credentials);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("From", twilioProperties.getWhatsappFrom());
        params.add("To", "whatsapp:" + normalizedPhone);
        params.add("Body", body);

        try {
            restTemplate.postForObject(url, new HttpEntity<>(params, headers), String.class);
            log.info("[WhatsApp] Mensagem enviada para {}", normalizedPhone);
        } catch (Exception e) {
            log.error("[WhatsApp] Falha ao enviar mensagem para {}: {}", normalizedPhone, e.getMessage());
        }
    }
}
