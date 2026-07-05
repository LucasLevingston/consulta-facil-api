package com.consultafacil.application.service;

import com.consultafacil.application.port.in.GoogleOAuthRedirectUseCase;
import com.consultafacil.core.config.GoogleOAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class GoogleOAuthRedirectService implements GoogleOAuthRedirectUseCase {

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";

    private final GoogleOAuthProperties googleProps;

    @Override
    public String buildAuthorizationUrl() {
        return UriComponentsBuilder.fromHttpUrl(GOOGLE_AUTH_URL)
                .queryParam("client_id", googleProps.getClientId())
                .queryParam("redirect_uri", googleProps.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("access_type", "online")
                .toUriString();
    }
}
