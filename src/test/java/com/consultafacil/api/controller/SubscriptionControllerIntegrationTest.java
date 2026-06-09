package com.consultafacil.api.controller;

import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.subscription.CheckoutResponseDTO;
import com.consultafacil.api.dto.subscription.SubscriptionResponseDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.application.service.SubscriptionService;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.ConsultaFacilApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class SubscriptionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name("Sub User")
                .email("subuser@example.com")
                .password("password123")
                .cpf("33344455566")
                .phone("11955556666")
                .birthDate(LocalDate.of(1995, 3, 20))
                .gender(Gender.FEMALE)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("subuser@example.com")
                        .password("password123")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        userToken = objectMapper.readTree(loginResponse).get("token").asText();
    }

    @Test
    void testGetMySubscriptionReturnsNoContentWhenNone() throws Exception {
        when(subscriptionService.getMySubscription(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/subscriptions/me")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetMySubscriptionReturnsActiveSubscription() throws Exception {
        SubscriptionResponseDTO sub = SubscriptionResponseDTO.builder()
                .id("sub-1")
                .planId("monthly")
                .status(SubscriptionStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(subscriptionService.getMySubscription(any())).thenReturn(Optional.of(sub));

        mockMvc.perform(get("/subscriptions/me")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId", equalTo("monthly")))
                .andExpect(jsonPath("$.status", equalTo("ACTIVE")));
    }

    @Test
    void testGetMySubscriptionRequiresAuth() throws Exception {
        mockMvc.perform(get("/subscriptions/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateCheckoutSuccess() throws Exception {
        CheckoutResponseDTO checkoutResponse = CheckoutResponseDTO.builder()
                .checkoutUrl("https://www.mercadopago.com.br/checkout/v1/redirect?pref_id=pref-123")
                .build();

        when(subscriptionService.createCheckout(any(), eq("monthly"), any(), any())).thenReturn(checkoutResponse);

        Map<String, String> body = Map.of("planId", "monthly");

        mockMvc.perform(post("/subscriptions/checkout")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl", containsString("mercadopago")));
    }

    @Test
    void testCreateCheckoutRequiresAuth() throws Exception {
        Map<String, String> body = Map.of("planId", "monthly");

        mockMvc.perform(post("/subscriptions/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testWebhookAlwaysReturns200() throws Exception {
        Map<String, Object> payload = Map.of("type", "payment", "data", Map.of("id", "pay-123"));

        mockMvc.perform(post("/subscriptions/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    @Test
    void testWebhookIgnoresUnknownEventType() throws Exception {
        Map<String, Object> payload = Map.of("type", "unknown_event", "data", Map.of("id", "x"));

        mockMvc.perform(post("/subscriptions/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    @Test
    void testWebhookSubscriptionPreapprovalRoutesToUseCase() throws Exception {
        Map<String, Object> payload = Map.of("type", "subscription_preapproval",
                "data", Map.of("id", "pre-abc123"));

        mockMvc.perform(post("/subscriptions/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        org.mockito.Mockito.verify(subscriptionService).handlePreapprovalWebhook("pre-abc123");
    }
}
