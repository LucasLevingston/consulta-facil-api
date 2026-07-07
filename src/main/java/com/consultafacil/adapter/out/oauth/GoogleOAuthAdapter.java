package com.consultafacil.adapter.out.oauth;

import com.consultafacil.core.config.GoogleOAuthProperties;
import com.consultafacil.core.exception.UnauthorizedException;
import com.consultafacil.domain.port.out.auth.GoogleOAuthPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuthAdapter implements GoogleOAuthPort {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL   = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final ObjectMapper objectMapper;
    private final GoogleOAuthProperties googleProps;

    @Override
    public GoogleUserInfo verifyIdToken(String idToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String json = restTemplate.getForObject(TOKENINFO_URL + idToken, String.class);
            JsonNode node = objectMapper.readTree(json);

            if (node.has("error_description")) {
                throw new UnauthorizedException("Invalid Google token: " + node.get("error_description").asText());
            }

            String aud = node.path("aud").asText("");
            if (!googleProps.getClientId().isBlank() && !aud.equals(googleProps.getClientId())) {
                throw new UnauthorizedException("Google token does not belong to this application.");
            }

            return extractUserInfo(node);

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("[GoogleOAuth] Failed to verify id_token: {}", e.getMessage());
            throw new UnauthorizedException("Falha ao validar token Google.");
        }
    }

    @Override
    public GoogleUserInfo exchangeCode(String code, String redirectUri) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", googleProps.getClientId());
            params.add("client_secret", googleProps.getClientSecret());
            params.add("redirect_uri", redirectUri);
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String tokenJson = restTemplate.postForObject(
                    TOKEN_ENDPOINT, new HttpEntity<>(params, headers), String.class);

            JsonNode tokenNode = objectMapper.readTree(tokenJson);
            if (tokenNode.has("error")) {
                throw new UnauthorizedException("Google code exchange failed: " + tokenNode.path("error_description").asText());
            }

            String accessToken = tokenNode.path("access_token").asText();
            HttpHeaders authHeader = new HttpHeaders();
            authHeader.setBearerAuth(accessToken);

            String userJson = restTemplate.exchange(
                    USERINFO_URL, org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(authHeader), String.class).getBody();

            JsonNode userNode = objectMapper.readTree(userJson);
            return extractUserInfo(userNode);

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("[GoogleOAuth] Code exchange failed: {}", e.getMessage());
            throw new UnauthorizedException("Falha ao autenticar com Google.");
        }
    }

    private GoogleUserInfo extractUserInfo(JsonNode node) {
        String sub     = node.path("sub").asText();
        String email   = node.path("email").asText();
        String name    = node.path("name").asText(email);
        String picture = node.path("picture").asText(null);
        return new GoogleUserInfo(sub, email, name, picture);
    }
}
