package com.consultafacil.api.controller;

import com.consultafacil.api.dto.coupon.CreateCouponDTO;
import com.consultafacil.api.dto.coupon.UpdateCouponDTO;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CouponControllerAdminIntegrationTest extends CouponControllerTestBase {

    @Test
    void adminCreateCoupon_successWithValidPayload() throws Exception {
        CreateCouponDTO dto = new CreateCouponDTO();
        dto.setCode("SAVE10");
        dto.setDescription("10% off");
        dto.setType(CouponType.PERCENT);
        dto.setValue(new BigDecimal("10.00"));

        mockMvc.perform(post("/admin/billing/coupons/codes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", equalTo("SAVE10")))
                .andExpect(jsonPath("$.type", equalTo("PERCENT")));
    }

    @Test
    void adminCreateCoupon_requiresAdmin() throws Exception {
        CreateCouponDTO dto = new CreateCouponDTO();
        dto.setCode("NOAUTH");
        dto.setType(CouponType.FIXED);
        dto.setValue(new BigDecimal("5.00"));

        mockMvc.perform(post("/admin/billing/coupons/codes")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreateCoupon_requiresAuth() throws Exception {
        CreateCouponDTO dto = new CreateCouponDTO();
        dto.setCode("NOAUTH2");
        dto.setType(CouponType.FIXED);
        dto.setValue(new BigDecimal("5.00"));

        mockMvc.perform(post("/admin/billing/coupons/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCreateCoupon_returns400WhenCodeBlank() throws Exception {
        CreateCouponDTO dto = new CreateCouponDTO();
        dto.setCode("");
        dto.setType(CouponType.PERCENT);
        dto.setValue(new BigDecimal("5.00"));

        mockMvc.perform(post("/admin/billing/coupons/codes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCreateCoupon_returns400WhenValueTooLow() throws Exception {
        CreateCouponDTO dto = new CreateCouponDTO();
        dto.setCode("ZEROVAL");
        dto.setType(CouponType.FIXED);
        dto.setValue(new BigDecimal("0.00"));

        mockMvc.perform(post("/admin/billing/coupons/codes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminListCoupons_returnsListAsAdmin() throws Exception {
        createCouponAndGetId("LISTTEST1");

        mockMvc.perform(get("/admin/billing/coupons/codes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void adminListCoupons_requiresAdmin() throws Exception {
        mockMvc.perform(get("/admin/billing/coupons/codes")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUpdateCoupon_successForAdmin() throws Exception {
        String id = createCouponAndGetId("UPDATEME");

        UpdateCouponDTO update = new UpdateCouponDTO();
        update.setDescription("Updated description");
        update.setStatus(CouponStatus.INACTIVE);

        mockMvc.perform(patch("/admin/billing/coupons/codes/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("INACTIVE")));
    }

    @Test
    void adminUpdateCoupon_requiresAdmin() throws Exception {
        UpdateCouponDTO update = new UpdateCouponDTO();
        update.setDescription("Hacked");

        mockMvc.perform(patch("/admin/billing/coupons/codes/any-id")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }
}
