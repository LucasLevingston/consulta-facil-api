package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.doctor.CreateDoctorDTO;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class DoctorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String doctorId;

    @BeforeEach
    void setUp() throws Exception {
        // Register admin user
        CreateUserDTO adminDTO = CreateUserDTO.builder()
                .name("Admin User")
                .email("admin@example.com")
                .password("admin123")
                .cpf("12345678901")
                .phone("11999999999")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminDTO)))
                .andExpect(status().isCreated());

        // Login admin
        LoginRequestDTO loginDTO = LoginRequestDTO.builder()
                .email("admin@example.com")
                .password("admin123")
                .build();

        String response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract token
        adminToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void testListDoctors() throws Exception {
        mockMvc.perform(get("/doctors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)));
    }

    @Test
    void testSearchDoctorsBySpecialty() throws Exception {
        mockMvc.perform(get("/doctors/search")
                .param("specialty", "Cardiologia")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
