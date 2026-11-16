package com.consultafacil.api.controller;

import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** GET /professionals/me/analytics — reuses the patient/professional fixtures from MedicalHistoryTestBase. */
class ProfessionalAnalyticsIntegrationTest extends MedicalHistoryTestBase {

    private void appointment(int daysAgo, AppointmentStatus status, AppointmentModality modality,
                             AppointmentPaymentStatus payment, String amount, Integer rating) {
        PatientProfile patient = patientProfileRepository.findById(patientProfileId).orElseThrow();
        ProfessionalProfile professional = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        appointmentRepository.saveAndFlush(Appointment.builder()
                .patient(patient).professional(professional)
                .scheduledAt(LocalDateTime.now().minusDays(daysAgo).withNano(0))
                .status(status).modality(modality)
                .paymentStatus(payment)
                .paymentAmount(amount == null ? null : new BigDecimal(amount))
                .rating(rating)
                .build());
    }

    @Test
    void professionalSeesAggregatedMetricsForTheLast30Days() throws Exception {
        appointment(3, AppointmentStatus.COMPLETED, AppointmentModality.IN_PERSON, AppointmentPaymentStatus.PAID, "200.00", 5);
        appointment(5, AppointmentStatus.CONFIRMED, AppointmentModality.ONLINE, AppointmentPaymentStatus.PAID, "100.00", 4);
        appointment(7, AppointmentStatus.CANCELED, AppointmentModality.IN_PERSON, AppointmentPaymentStatus.REFUNDED, "100.00", null);
        appointment(60, AppointmentStatus.COMPLETED, AppointmentModality.IN_PERSON, AppointmentPaymentStatus.PAID, "999.00", 1);

        mockMvc.perform(get("/professionals/me/analytics").param("period", "30d")
                        .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period", equalTo("30d")))
                .andExpect(jsonPath("$.revenue.total", closeTo(300.0, 0.001)))
                .andExpect(jsonPath("$.revenue.avgPerAppointment", closeTo(150.0, 0.001)))
                .andExpect(jsonPath("$.appointments.total", equalTo(3)))
                .andExpect(jsonPath("$.appointments.canceled", equalTo(1)))
                .andExpect(jsonPath("$.appointments.byModality.ONLINE", equalTo(1)))
                .andExpect(jsonPath("$.appointments.byWeekday", hasSize(7)))
                .andExpect(jsonPath("$.ratings.total", equalTo(2)))
                .andExpect(jsonPath("$.ratings.average", closeTo(4.5, 0.001)))
                .andExpect(jsonPath("$.ratings.distribution['5']", equalTo(1)))
                .andExpect(jsonPath("$.ratings.distribution['1']", equalTo(0)));
    }

    @Test
    void longerPeriodIncludesOlderAppointments() throws Exception {
        appointment(3, AppointmentStatus.COMPLETED, AppointmentModality.IN_PERSON, AppointmentPaymentStatus.PAID, "200.00", null);
        appointment(60, AppointmentStatus.COMPLETED, AppointmentModality.IN_PERSON, AppointmentPaymentStatus.PAID, "100.00", null);

        mockMvc.perform(get("/professionals/me/analytics").param("period", "90d")
                        .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments.total", equalTo(2)))
                .andExpect(jsonPath("$.revenue.total", closeTo(300.0, 0.001)));
    }

    @Test
    void defaultsTo30DaysWhenPeriodIsOmitted() throws Exception {
        mockMvc.perform(get("/professionals/me/analytics").header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period", equalTo("30d")))
                .andExpect(jsonPath("$.appointments.total", equalTo(0)));
    }

    @Test
    void unknownPeriodIsABadRequest() throws Exception {
        mockMvc.perform(get("/professionals/me/analytics").param("period", "7d")
                        .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patientsAreForbidden() throws Exception {
        mockMvc.perform(get("/professionals/me/analytics").header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCallersAreRejected() throws Exception {
        // /professionals/** is permitAll at URL level, so method security denies it (403) like the other /me routes.
        mockMvc.perform(get("/professionals/me/analytics"))
                .andExpect(status().isForbidden());
    }
}
