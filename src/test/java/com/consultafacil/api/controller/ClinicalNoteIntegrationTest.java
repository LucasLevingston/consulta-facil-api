package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.SaveClinicalNoteDTO;
import com.consultafacil.domain.entity.Appointment;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClinicalNoteIntegrationTest extends MedicalHistoryTestBase {

    @Test
    void testGetClinicalNoteNoContentWhenNotFilled() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(get("/appointments/" + appt.getId() + "/clinicalNote")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetClinicalNoteAfterSaveReturnsData() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/clinicalNote")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveClinicalNoteDTO.builder()
                        .clinicalNotes("Paciente estável").diagnosis("Hipertensão arterial")
                        .diagnosisCid("I10").build())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/appointments/" + appt.getId() + "/clinicalNote")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis", equalTo("Hipertensão arterial")))
                .andExpect(jsonPath("$.diagnosisCid", equalTo("I10")));
    }

    @Test
    void testSaveClinicalNoteAsProfessionalSuccess() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/clinicalNote")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveClinicalNoteDTO.builder()
                        .clinicalNotes("Paciente em bom estado geral").diagnosis("Cefaleia tensional")
                        .diagnosisCid("G44.2").prescription("Paracetamol 750mg 8/8h por 3 dias")
                        .treatmentPlan("Repouso e hidratação").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clinicalNotes", equalTo("Paciente em bom estado geral")))
                .andExpect(jsonPath("$.diagnosis", equalTo("Cefaleia tensional")))
                .andExpect(jsonPath("$.prescription", equalTo("Paracetamol 750mg 8/8h por 3 dias")));
    }

    @Test
    void testSaveClinicalNoteAsPatientFails() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/clinicalNote")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveClinicalNoteDTO.builder()
                        .clinicalNotes("Tentativa indevida").build())))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSaveClinicalNoteIdempotentUpdate() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/clinicalNote")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveClinicalNoteDTO.builder()
                        .clinicalNotes("Inicial").build())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/appointments/" + appt.getId() + "/clinicalNote")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveClinicalNoteDTO.builder()
                        .clinicalNotes("Atualizado").diagnosis("Nova hipótese").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clinicalNotes", equalTo("Atualizado")))
                .andExpect(jsonPath("$.diagnosis", equalTo("Nova hipótese")));
    }
}
