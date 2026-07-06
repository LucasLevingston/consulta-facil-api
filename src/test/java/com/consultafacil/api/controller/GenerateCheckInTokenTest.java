package com.consultafacil.api.controller;

import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GenerateCheckInTokenService — GET /appointments/{id}/checkin-token.
 */
class GenerateCheckInTokenTest extends QueueAndCheckInTestBase {

    @Test
    void testGetCheckInTokenSuccess() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CONFIRMED);

        mockMvc.perform(get("/appointments/" + appt.getId() + "/checkin-token")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.appointmentId", equalTo(appt.getId())));
    }

    @Test
    void testGetCheckInTokenForCanceledAppointmentFails() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CANCELED);

        mockMvc.perform(get("/appointments/" + appt.getId() + "/checkin-token")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCheckInTokenWrongPatientFails() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CONFIRMED);

        // professionalToken has PROFESSIONAL role — endpoint requires PATIENT
        mockMvc.perform(get("/appointments/" + appt.getId() + "/checkin-token")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isForbidden());
    }
}
