package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.enums.Gender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.DemoApplication;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class PatientProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String patientToken;
    private String patientUserId;

    @BeforeEach
    void setUp() throws Exception {
        CreateUserDTO patientDTO = CreateUserDTO.builder()
                .name("Patient User")
                .email("patient@example.com")
                .password("patient123")
                .cpf("12345678901")
                .phone("11999999999")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .build();

        String patientResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        patientUserId = objectMapper.readTree(patientResponse).get("id").asText();

        LoginRequestDTO loginDTO = LoginRequestDTO.builder()
                .email("patient@example.com")
                .password("patient123")
                .build();

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        patientToken = objectMapper.readTree(loginResponse).get("token").asText();
    }

    @Test
    void testGetMyProfile() throws Exception {
        mockMvc.perform(get("/patients/me")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.name", equalTo("Patient User")))
                .andExpect(jsonPath("$.email", equalTo("patient@example.com")));
    }

    @Test
    void testGetPatientProfileById() throws Exception {
        mockMvc.perform(get("/patients/" + patientUserId)
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", equalTo(patientUserId)));
    }

    @Test
    void testUpdateMyProfile() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("occupation", "Software Engineer");

        mockMvc.perform(put("/patients/me")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupation", equalTo("Software Engineer")));
    }

    @Test
    void testGetPatientMedicalRecordsNotFound() throws Exception {
        // Patient has no medical records initially — expect 404
        mockMvc.perform(get("/patients/" + patientUserId + "/medical-records")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdatePatientMedicalRecords() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("allergies", "Penicillin");
        updates.put("currentMedication", "Ibuprofen");
        updates.put("privacyConsent", true);

        mockMvc.perform(put("/patients/" + patientUserId + "/medical-records")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allergies", equalTo("Penicillin")))
                .andExpect(jsonPath("$.currentMedication", equalTo("Ibuprofen")))
                .andExpect(jsonPath("$.privacyConsent", equalTo(true)));
    }
}
