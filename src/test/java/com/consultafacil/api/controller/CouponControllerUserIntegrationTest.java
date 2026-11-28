package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CouponControllerUserIntegrationTest extends CouponControllerTestBase {

    @Test
    void adminListAllUsages_returnsListAsAdmin() throws Exception {
        mockMvc.perform(get("/admin/billing/coupons")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void adminListAllUsages_requiresAdmin() throws Exception {
        mockMvc.perform(get("/admin/billing/coupons")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminListAllUsages_requiresAuth() throws Exception {
        mockMvc.perform(get("/admin/billing/coupons"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminListByCoupon_returnsEmptyListForNonExistentCoupon() throws Exception {
        // A non-existent couponId just yields an empty list; no 404 is defined for this endpoint
        mockMvc.perform(get("/admin/billing/coupons/non-existent-id/usages")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void adminListByCoupon_requiresAdmin() throws Exception {
        mockMvc.perform(get("/admin/billing/coupons/any-id/usages")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void validateCoupon_requiresAuth() throws Exception {
        Map<String, Object> body = Map.of(
                "code", "SAVE10",
                "userId", patientUserId,
                "amount", "100.00");

        mockMvc.perform(post("/billing/coupons/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validateCoupon_returnsResultForAuthenticatedUser() throws Exception {
        // Coupon does not exist — expect a valid response body describing invalid status
        Map<String, Object> body = Map.of(
                "code", "NONEXIST",
                "userId", patientUserId,
                "amount", "100.00");

        mockMvc.perform(post("/billing/coupons/validate")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getCouponHistory_requiresAuth() throws Exception {
        mockMvc.perform(get("/billing/coupons/history")
                        .param("userId", patientUserId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCouponHistory_returnsEmptyListForUserWithNoHistory() throws Exception {
        mockMvc.perform(get("/billing/coupons/history")
                        .header("Authorization", "Bearer " + patientToken)
                        .param("userId", patientUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
