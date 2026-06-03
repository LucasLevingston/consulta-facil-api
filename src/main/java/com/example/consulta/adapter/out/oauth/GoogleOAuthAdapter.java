package com.example.consulta.adapter.out.oauth;

import com.example.consulta.core.exception.UnauthorizedException;
import com.example.consulta.domain.port.out.GoogleOAuthPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuthAdapter implements GoogleOAuthPort {

    private static final String TOKENINFO_URL =
            "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final ObjectMapper objectMapper;

    @Value("${google.client-id:}")
    private String expectedClientId;

    @Override
    public GoogleUserInfo verifyIdToken(String idToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String json = restTemplate.getForObject(TOKENINFO_URL + idToken, String.class);
            JsonNode node = objectMapper.readTree(json);

            if (node.has("error_description")) {
                throw new UnauthorizedException("Token Google inválido: " + node.get("error_description").asText());
            }

            String aud = node.path("aud").asText("");
            if (!expectedClientId.isBlank() && !aud.equals(expectedClientId)) {
                throw new UnauthorizedException("Token Google não pertence a esta aplicação.");
            }

            String sub     = node.path("sub").asText();
            String email   = node.path("email").asText();
            String name    = node.path("name").asText(email);
            String picture = node.path("picture").asText(null);

            return new GoogleUserInfo(sub, email, name, picture);

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("[GoogleOAuth] Failed to verify id_token: {}", e.getMessage());
            throw new UnauthorizedException("Falha ao validar token Google.");
        }
    }
}
