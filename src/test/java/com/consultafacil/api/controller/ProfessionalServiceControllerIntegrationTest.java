package com.consultafacil.api.controller;

import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.ProfessionalService;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.ProfessionalServiceRepository;
import com.consultafacil.domain.repository.UserRepository;
import com.consultafacil.ConsultaFacilApplication;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class ProfessionalServiceControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfessionalProfileRepository professionalProfileRepository;
    @Autowired private ProfessionalServiceRepository professionalServiceRepository;

    private String patientToken;
    private String professionalToken;
    private String professionalProfileId;
    private String professionalUserId;

    @BeforeEach
    void setUp() throws Exception {
        // Register patient
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Service Patient")
                        .email("svc.patient@test.com")
                        .password("pass1234")
                        .cpf("10000000001")
                        .phone("11900000001")
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .gender(Gender.FEMALE)
                        .build())))
                .andExpect(status().isCreated());

        patientToken = loginToken("svc.patient@test.com", "pass1234");

        // Register professional
        String docResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Service Doctor")
                        .email("svc.doctor@test.com")
                        .password("doc12345")
                        .cpf("10000000002")
                        .phone("11900000002")
                        .birthDate(LocalDate.of(1980, 3, 10))
                        .gender(Gender.MALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        professionalUserId = objectMapper.readTree(docResp).get("id").asText();
        User docUser = userRepository.findById(professionalUserId).orElseThrow();
        docUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(docUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(docUser)
                .specialty(Specialty.DERMATOLOGIA)
                .licenseNumber("CRM-SP-10001")
                .build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(profile).getId();

        professionalToken = loginToken("svc.doctor@test.com", "doc12345");
    }

    private String loginToken(String email, String password) throws Exception {
        String resp = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder().email(email).password(password).build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }

    private Map<String, Object> buildServicePayload(String name, double price, int duration, boolean requiresConsultation) {
        return Map.of("name", name, "price", price, "durationMinutes", duration,
                "requiresConsultation", requiresConsultation);
    }

    @Test
    void createService_shouldReturn201() throws Exception {
        mockMvc.perform(post("/professional-services")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildServicePayload("Limpeza de pele", 150.0, 60, false))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", equalTo("Limpeza de pele")))
                .andExpect(jsonPath("$.price", equalTo(150.0)))
                .andExpect(jsonPath("$.durationMinutes", equalTo(60)))
                .andExpect(jsonPath("$.active", equalTo(true)));
    }

    @Test
    void createService_byPatient_shouldReturn403() throws Exception {
        mockMvc.perform(post("/professional-services")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildServicePayload("Test", 100.0, 30, false))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getServices_shouldReturnActiveServices() throws Exception {
        ProfessionalProfile profile = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        professionalServiceRepository.saveAndFlush(ProfessionalService.builder()
                .professional(profile).name("Botox").price(new BigDecimal("500.00")).durationMinutes(45).build());
        professionalServiceRepository.saveAndFlush(ProfessionalService.builder()
                .professional(profile).name("Peeling").price(new BigDecimal("200.00")).durationMinutes(30).build());

        mockMvc.perform(get("/professional-services/" + professionalProfileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Botox", "Peeling")));
    }

    @Test
    void getServices_excludesInactiveServices() throws Exception {
        ProfessionalProfile profile = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        professionalServiceRepository.saveAndFlush(ProfessionalService.builder()
                .professional(profile).name("Ativo").price(new BigDecimal("100.00")).durationMinutes(30).build());
        ProfessionalService inactive = ProfessionalService.builder()
                .professional(profile).name("Inativo").price(new BigDecimal("100.00")).durationMinutes(30)
                .active(false).build();
        professionalServiceRepository.saveAndFlush(inactive);

        mockMvc.perform(get("/professional-services/" + professionalProfileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo("Ativo")));
    }

    @Test
    void updateService_shouldReturn200() throws Exception {
        String createResp = mockMvc.perform(post("/professional-services")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildServicePayload("Original", 100.0, 30, false))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String serviceId = objectMapper.readTree(createResp).get("id").asText();

        mockMvc.perform(put("/professional-services/" + serviceId)
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Atualizado", "price", 250.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Atualizado")))
                .andExpect(jsonPath("$.price", equalTo(250.0)));
    }

    @Test
    void deactivateService_shouldReturn204AndBeExcludedFromList() throws Exception {
        String createResp = mockMvc.perform(post("/professional-services")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildServicePayload("Para desativar", 80.0, 20, false))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String serviceId = objectMapper.readTree(createResp).get("id").asText();

        mockMvc.perform(delete("/professional-services/" + serviceId)
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/professional-services/" + professionalProfileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void setConsultationPrice_shouldReturn200WithPrice() throws Exception {
        mockMvc.perform(put("/professionals/me/consultation-price")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("price", 300.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultationPrice", equalTo(300.0)));
    }

    @Test
    void setConsultationPrice_byPatient_shouldReturn403() throws Exception {
        mockMvc.perform(put("/professionals/me/consultation-price")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("price", 100.0))))
                .andExpect(status().isForbidden());
    }
}
