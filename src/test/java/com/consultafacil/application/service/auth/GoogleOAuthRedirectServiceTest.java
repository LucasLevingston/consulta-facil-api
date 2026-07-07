package com.consultafacil.application.service.auth;

import com.consultafacil.core.config.GoogleOAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleOAuthRedirectServiceTest {

    GoogleOAuthProperties googleProps;
    GoogleOAuthRedirectService service;

    @BeforeEach
    void setUp() {
        googleProps = new GoogleOAuthProperties();
        googleProps.setClientId("client-123");
        googleProps.setRedirectUri("http://localhost:8080/v1/auth/google/callback");
        service = new GoogleOAuthRedirectService(googleProps);
    }

    @Test
    void buildAuthorizationUrl_includesClientIdAndRedirectUri() {
        String url = service.buildAuthorizationUrl();

        assertThat(url).startsWith("https://accounts.google.com/o/oauth2/v2/auth");
        assertThat(url).contains("client_id=client-123");
        assertThat(url).contains("redirect_uri=http://localhost:8080/v1/auth/google/callback");
    }

    @Test
    void buildAuthorizationUrl_requestsCodeResponseTypeAndOpenIdScopes() {
        String url = service.buildAuthorizationUrl();

        assertThat(url).contains("response_type=code");
        assertThat(url).contains("scope=openid");
        assertThat(url).contains("access_type=online");
    }
}
