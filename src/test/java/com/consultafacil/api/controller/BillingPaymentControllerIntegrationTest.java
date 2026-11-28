package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.payment.CreateBillingPaymentDTO;
import com.consultafacil.domain.enums.PaymentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BillingPaymentControllerIntegrationTest extends BillingPaymentControllerTestBase {

    private CreateBillingPaymentDTO validDto() {
        CreateBillingPaymentDTO dto = new CreateBillingPaymentDTO();
        dto.setPaymentType(PaymentType.CONSULTATION);
        dto.setAmount(new BigDecimal("150.00"));
        dto.setPayerId(patientUserId);
        dto.setPayerName("Billing Patient");
        dto.setPayerEmail("billing-patient@example.com");
        dto.setDescription("Test consultation payment");
        return dto;
    }

    @Test
    void createPayment_returnsOkWithValidPayload() throws Exception {
        // The endpoint is protected by anyRequest().authenticated()
        mockMvc.perform(post("/billing/payments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.paymentType", equalTo("CONSULTATION")))
                .andExpect(jsonPath("$.amount", equalTo(150.00)));
    }

    @Test
    void createPayment_requiresAuth() throws Exception {
        mockMvc.perform(post("/billing/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPayment_returns400WhenAmountMissing() throws Exception {
        CreateBillingPaymentDTO dto = new CreateBillingPaymentDTO();
        dto.setPaymentType(PaymentType.CONSULTATION);
        // amount not set
        dto.setPayerId(patientUserId);

        mockMvc.perform(post("/billing/payments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_returns400WhenPaymentTypeMissing() throws Exception {
        CreateBillingPaymentDTO dto = new CreateBillingPaymentDTO();
        dto.setAmount(new BigDecimal("100.00"));
        dto.setPayerId(patientUserId);

        mockMvc.perform(post("/billing/payments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_returns404ForNonExistentPayment() throws Exception {
        mockMvc.perform(get("/billing/payments/non-existent-id")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_requiresAuth() throws Exception {
        mockMvc.perform(get("/billing/payments/any-id"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_returnsPaymentWhenExists() throws Exception {
        // Create first
        String createResp = mockMvc.perform(post("/billing/payments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(createResp).get("id").asText();

        mockMvc.perform(get("/billing/payments/" + id)
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(id)));
    }

    @Test
    void myPayments_requiresAuth() throws Exception {
        mockMvc.perform(get("/billing/payments/me")
                        .param("payerId", patientUserId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void myPayments_returnsListForPayer() throws Exception {
        // Create a payment first
        mockMvc.perform(post("/billing/payments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/billing/payments/me")
                        .header("Authorization", "Bearer " + patientToken)
                        .param("payerId", patientUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void adminListAll_returnsListAsAdmin() throws Exception {
        mockMvc.perform(get("/admin/billing/payments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void adminListAll_requiresAdmin() throws Exception {
        mockMvc.perform(get("/admin/billing/payments")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminListAll_requiresAuth() throws Exception {
        mockMvc.perform(get("/admin/billing/payments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void webhook_nonExistentPaymentReturns4xx() throws Exception {
        // Payment not found → service throws ResourceNotFoundException → 400 or 404
        Map<String, Object> payload = Map.of(
                "gateway_payment_id", "gw-test-123",
                "status", "PAID");

        mockMvc.perform(post("/billing/payments/webhook")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void webhook_requiresAuth() throws Exception {
        // The webhook path /billing/payments/webhook is not in the SecurityConfig permitAll list
        mockMvc.perform(post("/billing/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
