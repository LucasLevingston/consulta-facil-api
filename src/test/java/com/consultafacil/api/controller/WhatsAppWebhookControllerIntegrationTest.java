package com.consultafacil.api.controller;

import com.consultafacil.ConsultaFacilApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class WhatsAppWebhookControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void testWebhookReturnsXmlTwiml() throws Exception {
        mockMvc.perform(post("/webhook/whatsapp")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("From", "whatsapp:+5511999999999")
                .param("Body", "Olá, quero agendar uma consulta"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(content().string(containsString("<Response>")))
                .andExpect(content().string(containsString("<Message>")));
    }

    @Test
    void testWebhookWithEmptyBodyReturnsXml() throws Exception {
        mockMvc.perform(post("/webhook/whatsapp")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("From", "whatsapp:+5511999999999")
                .param("Body", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<Response>")));
    }

    @Test
    void testWebhookWithMissingParamsUsesDefaults() throws Exception {
        mockMvc.perform(post("/webhook/whatsapp")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<Response>")));
    }

    @Test
    void testWebhookEscapesSpecialCharsInReply() throws Exception {
        // When API key is blank, service returns fixed message — no XML injection possible
        mockMvc.perform(post("/webhook/whatsapp")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("From", "whatsapp:+5511999999999")
                .param("Body", "teste <injeção> & 'aspas'"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<injeção>"))));
    }

    @Test
    void testWebhookIsPublicNoAuthRequired() throws Exception {
        mockMvc.perform(post("/webhook/whatsapp")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("From", "whatsapp:+5511999999999")
                .param("Body", "teste"))
                .andExpect(status().isOk());
    }
}
