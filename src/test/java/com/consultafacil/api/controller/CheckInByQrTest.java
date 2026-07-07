package com.consultafacil.api.controller;

import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CheckInByQrService — POST /appointments/checkin.
 */
class CheckInByQrTest extends QueueAndCheckInTestBase {

    @Test
    void testCheckInByQrSuccess() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CONFIRMED);

        // Get token first
        String tokenResp = mockMvc.perform(get("/appointments/" + appt.getId() + "/checkin-token")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String qrToken = objectMapper.readTree(tokenResp).get("token").asText();

        // Use token to check in (requires PROFESSIONAL/RECEPTIONIST/ADMIN role)
        mockMvc.perform(post("/appointments/checkin")
                .header("Authorization", "Bearer " + professionalToken)
                .param("token", qrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("CHECKED_IN")))
                .andExpect(jsonPath("$.checkedInAt", notNullValue()));
    }

    @Test
    void testCheckInByQrWithInvalidTokenFails() throws Exception {
        mockMvc.perform(post("/appointments/checkin")
                .header("Authorization", "Bearer " + professionalToken)
                .param("token", "invalid.jwt.token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCheckInByQrAlreadyCheckedInFails() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CONFIRMED);

        String tokenResp = mockMvc.perform(get("/appointments/" + appt.getId() + "/checkin-token")
                .header("Authorization", "Bearer " + patientToken))
                .andReturn().getResponse().getContentAsString();
        String qrToken = objectMapper.readTree(tokenResp).get("token").asText();

        // First check-in succeeds
        mockMvc.perform(post("/appointments/checkin")
                .header("Authorization", "Bearer " + professionalToken)
                .param("token", qrToken))
                .andExpect(status().isOk());

        // Second check-in fails (status now CHECKED_IN, not CONFIRMED/PENDING)
        mockMvc.perform(post("/appointments/checkin")
                .header("Authorization", "Bearer " + professionalToken)
                .param("token", qrToken))
                .andExpect(status().isBadRequest());
    }
}
