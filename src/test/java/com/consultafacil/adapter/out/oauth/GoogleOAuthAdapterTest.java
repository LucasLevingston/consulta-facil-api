package com.consultafacil.adapter.out.oauth;

import com.consultafacil.core.config.GoogleOAuthProperties;
import com.consultafacil.core.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthAdapterTest {

    GoogleOAuthAdapter adapter;
    GoogleOAuthProperties googleProps;

    @BeforeEach
    void setUp() {
        googleProps = new GoogleOAuthProperties();
        adapter = new GoogleOAuthAdapter(new ObjectMapper(), googleProps);
    }

    @Test
    void verifyIdToken_validToken_shouldReturnUserInfo() throws Exception {
        String json = """
                {"sub":"123","email":"user@gmail.com","name":"John Doe","picture":"https://pic.url"}
                """;
        try (MockedConstruction<RestTemplate> mock = mockConstruction(RestTemplate.class,
                (rt, ctx) -> when(rt.getForObject(anyString(), eq(String.class))).thenReturn(json))) {

            var result = adapter.verifyIdToken("valid-token");

            assertThat(result.sub()).isEqualTo("123");
            assertThat(result.email()).isEqualTo("user@gmail.com");
            assertThat(result.name()).isEqualTo("John Doe");
            assertThat(result.picture()).isEqualTo("https://pic.url");
        }
    }

    @Test
    void verifyIdToken_noName_shouldFallbackToEmail() throws Exception {
        String json = """
                {"sub":"123","email":"user@gmail.com"}
                """;
        try (MockedConstruction<RestTemplate> mock = mockConstruction(RestTemplate.class,
                (rt, ctx) -> when(rt.getForObject(anyString(), eq(String.class))).thenReturn(json))) {

            var result = adapter.verifyIdToken("valid-token");

            assertThat(result.name()).isEqualTo("user@gmail.com");
        }
    }

    @Test
    void verifyIdToken_googleReturnsErrorDescription_shouldThrowUnauthorized() {
        String json = """
                {"error_description":"Token has been expired or revoked."}
                """;
        try (MockedConstruction<RestTemplate> mock = mockConstruction(RestTemplate.class,
                (rt, ctx) -> when(rt.getForObject(anyString(), eq(String.class))).thenReturn(json))) {

            assertThatThrownBy(() -> adapter.verifyIdToken("expired-token"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid Google token");
        }
    }

    @Test
    void verifyIdToken_clientIdMismatch_shouldThrowUnauthorized() throws Exception {
        googleProps.setClientId("correct-client-id");
        String json = """
                {"sub":"123","email":"user@gmail.com","aud":"different-client-id"}
                """;
        try (MockedConstruction<RestTemplate> mock = mockConstruction(RestTemplate.class,
                (rt, ctx) -> when(rt.getForObject(anyString(), eq(String.class))).thenReturn(json))) {

            assertThatThrownBy(() -> adapter.verifyIdToken("wrong-client-token"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("does not belong to this application");
        }
    }

    @Test
    void verifyIdToken_restTemplateThrows_shouldThrowUnauthorized() {
        try (MockedConstruction<RestTemplate> mock = mockConstruction(RestTemplate.class,
                (rt, ctx) -> when(rt.getForObject(anyString(), eq(String.class)))
                        .thenThrow(new RuntimeException("Network error")))) {

            assertThatThrownBy(() -> adapter.verifyIdToken("any-token"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Falha ao validar token Google");
        }
    }

    @Test
    void verifyIdToken_blankClientId_shouldSkipAudCheck() {
        String json = """
                {"sub":"123","email":"user@gmail.com","aud":"any-client"}
                """;
        try (MockedConstruction<RestTemplate> mock = mockConstruction(RestTemplate.class,
                (rt, ctx) -> when(rt.getForObject(anyString(), eq(String.class))).thenReturn(json))) {

            var result = adapter.verifyIdToken("any-token");

            assertThat(result.sub()).isEqualTo("123");
        }
    }
}
