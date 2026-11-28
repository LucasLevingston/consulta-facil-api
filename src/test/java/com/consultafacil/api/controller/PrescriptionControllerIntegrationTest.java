package com.consultafacil.api.controller;

import com.consultafacil.api.dto.prescription.IssuePrescriptionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PrescriptionControllerIntegrationTest extends PrescriptionControllerTestBase {

    @Test
    void issuePrescription_professionalCanIssueForOwnAppointment() throws Exception {
        IssuePrescriptionRequest req = new IssuePrescriptionRequest(
                appointmentId,
                "Amoxicillin 500mg every 8h for 7 days",
                "Take with food",
                LocalDate.now().plusDays(30));

        mockMvc.perform(post("/prescriptions")
                        .header("Authorization", "Bearer " + professionalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.medications", equalTo("Amoxicillin 500mg every 8h for 7 days")))
                .andExpect(jsonPath("$.appointmentId", equalTo(appointmentId)));
    }

    @Test
    void issuePrescription_requiresProfessionalRole() throws Exception {
        IssuePrescriptionRequest req = new IssuePrescriptionRequest(
                appointmentId, "Some drug", null, null);

        // A patient must receive 403
        mockMvc.perform(post("/prescriptions")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void issuePrescription_requiresAuth() throws Exception {
        IssuePrescriptionRequest req = new IssuePrescriptionRequest(
                appointmentId, "Some drug", null, null);

        mockMvc.perform(post("/prescriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void issuePrescription_returns400WhenAppointmentIdBlank() throws Exception {
        // appointmentId is @NotBlank
        IssuePrescriptionRequest req = new IssuePrescriptionRequest(
                "", "Some drug", null, null);

        mockMvc.perform(post("/prescriptions")
                        .header("Authorization", "Bearer " + professionalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void issuePrescription_returns400WhenMedicationsBlank() throws Exception {
        // medications is @NotBlank
        IssuePrescriptionRequest req = new IssuePrescriptionRequest(
                appointmentId, "", null, null);

        mockMvc.perform(post("/prescriptions")
                        .header("Authorization", "Bearer " + professionalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void issuePrescription_returns404WhenAppointmentNotFound() throws Exception {
        IssuePrescriptionRequest req = new IssuePrescriptionRequest(
                "non-existent-appointment-id", "Aspirin 100mg", null, null);

        mockMvc.perform(post("/prescriptions")
                        .header("Authorization", "Bearer " + professionalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listMine_patientCanListOwnPrescriptions() throws Exception {
        // Issue a prescription first so the list is non-empty
        IssuePrescriptionRequest req = new IssuePrescriptionRequest(
                appointmentId, "Paracetamol 750mg", null, null);
        mockMvc.perform(post("/prescriptions")
                        .header("Authorization", "Bearer " + professionalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/prescriptions/mine")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(java.util.List.class)));
    }

    @Test
    void listMine_requiresAuth() throws Exception {
        mockMvc.perform(get("/prescriptions/mine"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listByAppointment_authenticatedUserCanListPrescriptions() throws Exception {
        // Issue one so the list has data
        IssuePrescriptionRequest req = new IssuePrescriptionRequest(
                appointmentId, "Metformin 500mg", null, null);
        mockMvc.perform(post("/prescriptions")
                        .header("Authorization", "Bearer " + professionalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/prescriptions/appointment/" + appointmentId)
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void listByAppointment_requiresAuth() throws Exception {
        mockMvc.perform(get("/prescriptions/appointment/" + appointmentId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void download_returns404WhenPrescriptionNotFound() throws Exception {
        // The download endpoint returns 404 when prescription is not found or pdfUrl is null
        mockMvc.perform(get("/prescriptions/non-existent-id/download")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void download_requiresAuth() throws Exception {
        mockMvc.perform(get("/prescriptions/any-id/download"))
                .andExpect(status().isUnauthorized());
    }
}
