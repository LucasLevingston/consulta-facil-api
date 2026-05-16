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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateUserDTO createUserDTO;
    private LoginRequestDTO loginRequestDTO;

    @BeforeEach
    void setUp() {
        createUserDTO = CreateUserDTO.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password123")
                .cpf("12345678901")
                .phone("11999999999")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .build();

        loginRequestDTO = LoginRequestDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    void testRegisterUser() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email", equalTo("test@example.com")))
                .andExpect(jsonPath("$.name", equalTo("Test User")))
                .andExpect(jsonPath("$.role", equalTo("USER")));
    }

    @Test
    void testLoginUser() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", equalTo("test@example.com")));
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());

        // Duplicate email returns 409 Conflict
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("User")));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());

        LoginRequestDTO wrongPassword = LoginRequestDTO.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPassword)))
                .andExpect(status().isUnauthorized());
    }
}
