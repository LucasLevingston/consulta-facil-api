package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.SaveMedicalHistoryDTO;
import com.consultafacil.domain.entity.Appointment;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnamnesisIntegrationTest extends MedicalHistoryTestBase {

    @Test
    void testGetAnamnesisNoContentWhenNotFilled() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(get("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetAnamnesisAfterSaveReturnsData() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveMedicalHistoryDTO.builder()
                        .chiefComplaint("Dor de cabeça").allergies("Dipirona").build())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiefComplaint", equalTo("Dor de cabeça")))
                .andExpect(jsonPath("$.allergies", equalTo("Dipirona")));
    }

    @Test
    void testSaveAnamnesisAsPatientSuccess() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveMedicalHistoryDTO.builder()
                        .chiefComplaint("Tontura").currentMedications("Losartana 50mg")
                        .allergies("Nenhuma").medicalHistory("Hipertensão").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiefComplaint", equalTo("Tontura")))
                .andExpect(jsonPath("$.currentMedications", equalTo("Losartana 50mg")));
    }

    @Test
    void testSaveAnamnesisAsProfessionalSuccess() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveMedicalHistoryDTO.builder()
                        .chiefComplaint("Anotado pelo médico")
                        .observations("Paciente apresenta bom estado geral").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiefComplaint", equalTo("Anotado pelo médico")));
    }

    @Test
    void testSaveAnamnesisIdempotentUpdate() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveMedicalHistoryDTO.builder()
                        .chiefComplaint("Primeiro preenchimento").build())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveMedicalHistoryDTO.builder()
                        .chiefComplaint("Atualização").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiefComplaint", equalTo("Atualização")));
    }
}
