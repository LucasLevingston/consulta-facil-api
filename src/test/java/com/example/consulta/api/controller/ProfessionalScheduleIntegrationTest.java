package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.enums.ProfessionalProfileStatus;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import com.example.consulta.domain.repository.UserRepository;
import com.example.demo.DemoApplication;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class ProfessionalScheduleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessionalProfileRepository professionalProfileRepository;

    private String professionalToken;
    private String professionalProfileId;

    @BeforeEach
    void setUp() throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name("Dra. Ana Lima")
                .email("ana.schedule@example.com")
                .password("senha123")
                .cpf("11122233344")
                .phone("11977776666")
                .birthDate(LocalDate.of(1985, 3, 15))
                .gender(Gender.FEMALE)
                .build();

        String regResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String userId = objectMapper.readTree(regResponse).get("id").asText();

        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(user);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user)
                .profession("Médico")
                .specialty("Cardiologia")
                .licenseNumber("CRM-SP-99001")
                .status(ProfessionalProfileStatus.ACTIVE)
                .build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(profile).getId();

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder()
                                .email("ana.schedule@example.com")
                                .password("senha123")
                                .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        professionalToken = objectMapper.readTree(loginResponse).get("token").asText();
    }

    @Test
    void getScheduleByProfessionalId_semDados_retornaListaVazia() throws Exception {
        mockMvc.perform(get("/professionals/" + professionalProfileId + "/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getMySchedule_semDados_retornaListaVazia() throws Exception {
        mockMvc.perform(get("/professionals/me/schedule")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getMySchedule_semAutenticacao_retorna403() throws Exception {
        // /professionals/** is permitAll() at URL level; @PreAuthorize fires and returns 403
        mockMvc.perform(get("/professionals/me/schedule"))
                .andExpect(status().isForbidden());
    }

    @Test
    void saveMySchedule_comUmDia_retornaHorarioSalvo() throws Exception {
        List<Map<String, Object>> payload = List.of(Map.of(
                "dayOfWeek", "MONDAY",
                "startTime", "08:00",
                "endTime", "17:00",
                "consultationDurationMinutes", 30,
                "breakBetweenConsultationsMinutes", 10,
                "isActive", true
        ));

        mockMvc.perform(put("/professionals/me/schedule")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dayOfWeek", equalTo("MONDAY")))
                .andExpect(jsonPath("$[0].startTime", equalTo("08:00")))
                .andExpect(jsonPath("$[0].endTime", equalTo("17:00")))
                .andExpect(jsonPath("$[0].consultationDurationMinutes", equalTo(30)))
                .andExpect(jsonPath("$[0].breakBetweenConsultationsMinutes", equalTo(10)))
                .andExpect(jsonPath("$[0].isActive", equalTo(true)))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].professionalProfileId", equalTo(professionalProfileId)));
    }

    @Test
    void saveMySchedule_semanaCompleta_retorna7Registros() throws Exception {
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        List<Map<String, Object>> payload = java.util.Arrays.stream(days)
                .map(day -> Map.<String, Object>of(
                        "dayOfWeek", day,
                        "startTime", "09:00",
                        "endTime", "18:00",
                        "consultationDurationMinutes", 45,
                        "breakBetweenConsultationsMinutes", 15,
                        "isActive", !day.equals("SATURDAY") && !day.equals("SUNDAY")
                ))
                .toList();

        mockMvc.perform(put("/professionals/me/schedule")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)));
    }

    @Test
    void saveMySchedule_entaoGetPublico_retornaHorarios() throws Exception {
        List<Map<String, Object>> payload = List.of(
                Map.of("dayOfWeek", "TUESDAY", "startTime", "07:30", "endTime", "12:00",
                        "consultationDurationMinutes", 20, "breakBetweenConsultationsMinutes", 5, "isActive", true)
        );

        mockMvc.perform(put("/professionals/me/schedule")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/professionals/" + professionalProfileId + "/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dayOfWeek", equalTo("TUESDAY")))
                .andExpect(jsonPath("$[0].startTime", equalTo("07:30")));
    }

    @Test
    void saveMySchedule_upsert_atualizaDiaExistente() throws Exception {
        List<Map<String, Object>> primeira = List.of(Map.of(
                "dayOfWeek", "WEDNESDAY", "startTime", "08:00", "endTime", "16:00",
                "consultationDurationMinutes", 30, "breakBetweenConsultationsMinutes", 0, "isActive", true
        ));

        mockMvc.perform(put("/professionals/me/schedule")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(primeira)))
                .andExpect(status().isOk());

        List<Map<String, Object>> segunda = List.of(Map.of(
                "dayOfWeek", "WEDNESDAY", "startTime", "10:00", "endTime", "18:00",
                "consultationDurationMinutes", 60, "breakBetweenConsultationsMinutes", 15, "isActive", false
        ));

        mockMvc.perform(put("/professionals/me/schedule")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(segunda)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/professionals/me/schedule")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].startTime", equalTo("10:00")))
                .andExpect(jsonPath("$[0].endTime", equalTo("18:00")))
                .andExpect(jsonPath("$[0].consultationDurationMinutes", equalTo(60)))
                .andExpect(jsonPath("$[0].isActive", equalTo(false)));
    }

    @Test
    void saveMySchedule_semAutenticacao_retorna403() throws Exception {
        // /professionals/** is permitAll() at URL level; @PreAuthorize fires and returns 403
        List<Map<String, Object>> payload = List.of(Map.of(
                "dayOfWeek", "FRIDAY", "startTime", "08:00", "endTime", "17:00",
                "consultationDurationMinutes", 30, "breakBetweenConsultationsMinutes", 0, "isActive", true
        ));

        mockMvc.perform(put("/professionals/me/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void saveMySchedule_profissionalInexistente_retorna404() throws Exception {
        // Register a plain PATIENT user — they have no professional profile
        CreateUserDTO patientDto = CreateUserDTO.builder()
                .name("Paciente Sem Perfil")
                .email("paciente.sem@example.com")
                .password("senha123")
                .cpf("55566677788")
                .phone("11955554444")
                .birthDate(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDto)))
                .andExpect(status().isCreated());

        // Promote to PROFESSIONAL role without creating a profile
        User patient = userRepository.findByEmail("paciente.sem@example.com").orElseThrow();
        patient.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(patient);

        String loginResp = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder()
                                .email("paciente.sem@example.com")
                                .password("senha123")
                                .build())))
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(loginResp).get("token").asText();

        List<Map<String, Object>> payload = List.of(Map.of(
                "dayOfWeek", "MONDAY", "startTime", "08:00", "endTime", "17:00",
                "consultationDurationMinutes", 30, "breakBetweenConsultationsMinutes", 0, "isActive", true
        ));

        mockMvc.perform(put("/professionals/me/schedule")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
    }
}
