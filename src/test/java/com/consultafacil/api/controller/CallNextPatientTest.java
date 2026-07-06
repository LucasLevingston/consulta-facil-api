package com.consultafacil.api.controller;

import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CallNextPatientService — PUT /appointments/{id}/call.
 */
class CallNextPatientTest extends QueueAndCheckInTestBase {

    @Test
    void testCallNextPatientSuccess() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CHECKED_IN);

        mockMvc.perform(put("/appointments/" + appt.getId() + "/call")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("IN_PROGRESS")))
                .andExpect(jsonPath("$.calledAt", notNullValue()));
    }

    @Test
    void testCallNextPatientNotCheckedInFails() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CONFIRMED);

        mockMvc.perform(put("/appointments/" + appt.getId() + "/call")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCallNextPatientWrongProfessionalFails() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CHECKED_IN);

        // Patient token has PATIENT role — endpoint requires PROFESSIONAL/ADMIN
        mockMvc.perform(put("/appointments/" + appt.getId() + "/call")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }
}
