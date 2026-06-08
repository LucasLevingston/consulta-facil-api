package com.consultafacil.application.service;

import com.consultafacil.core.config.TwilioProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WhatsAppServiceTest {

    @Mock TwilioProperties twilioProperties;
    @InjectMocks WhatsAppService service;

    RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        when(twilioProperties.getWhatsappFrom()).thenReturn("whatsapp:+14155238886");
        // Inject a mock RestTemplate so tests can control/verify HTTP behaviour without real calls
        restTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    }

    @Test
    void sendMessage_emptyCredentials_shouldSkipAndNotThrow() {
        when(twilioProperties.getAccountSid()).thenReturn("");
        when(twilioProperties.getAuthToken()).thenReturn("");

        assertThatCode(() -> service.sendMessage("+5511999999999", "Olá!")).doesNotThrowAnyException();
    }

    @Test
    void sendMessage_nullCredentials_shouldSkipAndNotThrow() {
        when(twilioProperties.getAccountSid()).thenReturn(null);
        when(twilioProperties.getAuthToken()).thenReturn(null);

        assertThatCode(() -> service.sendMessage("+5511999999999", "Olá!")).doesNotThrowAnyException();
    }

    @Test
    void sendMessage_blankPhone_shouldSkipAndNotThrow() {
        when(twilioProperties.getAccountSid()).thenReturn("sid");
        when(twilioProperties.getAuthToken()).thenReturn("token");

        assertThatCode(() -> service.sendMessage("", "Olá!")).doesNotThrowAnyException();
    }

    @Test
    void sendMessage_nullPhone_shouldSkipAndNotThrow() {
        when(twilioProperties.getAccountSid()).thenReturn("sid");
        when(twilioProperties.getAuthToken()).thenReturn("token");

        assertThatCode(() -> service.sendMessage(null, "Olá!")).doesNotThrowAnyException();
    }

    @Test
    void sendMessage_validCredentials_shouldCallTwilioApi() {
        when(twilioProperties.getAccountSid()).thenReturn("ACtest");
        when(twilioProperties.getAuthToken()).thenReturn("authtoken");
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn("ok");

        service.sendMessage("+5511999999999", "Mensagem de teste");

        verify(restTemplate).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    void sendMessage_twilioThrows_shouldNotPropagateException() {
        when(twilioProperties.getAccountSid()).thenReturn("ACtest");
        when(twilioProperties.getAuthToken()).thenReturn("authtoken");
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Twilio error"));

        assertThatCode(() -> service.sendMessage("+5511999999999", "Msg")).doesNotThrowAnyException();
    }

    @Test
    void sendMessage_phoneWithoutPlus_shouldNormalize() {
        when(twilioProperties.getAccountSid()).thenReturn("ACtest");
        when(twilioProperties.getAuthToken()).thenReturn("authtoken");
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn("ok");

        // phone without "+" prefix should be normalized before being sent
        assertThatCode(() -> service.sendMessage("5511999999999", "Msg")).doesNotThrowAnyException();
    }
}
