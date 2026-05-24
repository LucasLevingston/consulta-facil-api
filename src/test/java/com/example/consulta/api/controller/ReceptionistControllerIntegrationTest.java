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
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class ReceptionistControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfessionalProfileRepository professionalProfileRepository;
    private String ownerToken;
    private String ownerUserId;
    private String receptionistEmail;
    private String clinicId;

    @BeforeEach
    void setUp() throws Exception {
        CreateUserDTO ownerDTO = CreateUserDTO.builder()
                .name("Clinic Owner Recep").email("recep.owner.unique@test.com")
                .password("owner1234").cpf("63312300000")
                .phone("11900000060").birthDate(LocalDate.of(1980, 1, 1)).gender(Gender.MALE).build();

        String regResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ownerUserId = objectMapper.readTree(regResp).get("id").asText();
        User ownerUser = userRepository.findById(ownerUserId).orElseThrow();
        ownerUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(ownerUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(ownerUser).specialty("Cardiologia")
                .licenseNumber("CRM-SP-63300").status(ProfessionalProfileStatus.ACTIVE).build();
        professionalProfileRepository.saveAndFlush(profile);

        ownerToken = loginAndGetToken("recep.owner.unique@test.com", "owner1234");

        String clinicJson = mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Receptionist Test Clinic",
                        "phone", "11900000061",
                        "address", "Rua Teste",
                        "city", "São Paulo",
                        "state", "SP",
                        "zipCode", "01310100"
                ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        clinicId = objectMapper.readTree(clinicJson).get("id").asText();

        CreateUserDTO receptionistDTO = CreateUserDTO.builder()
                .name("Receptionist User").email("recep.user.unique@test.com")
                .password("recep1234").cpf("63312300001")
                .phone("11900000062").birthDate(LocalDate.of(1995, 5, 10)).gender(Gender.FEMALE).build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receptionistDTO)))
                .andExpect(status().isCreated());

        receptionistEmail = "recep.user.unique@test.com";
    }

    @Test
    void inviteReceptionist_shouldReturn201AndAssignRole() throws Exception {
        mockMvc.perform(post("/clinics/{clinicId}/receptionists", clinicId)
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", receptionistEmail))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(receptionistEmail))
                .andExpect(jsonPath("$.id").isString());

        User receptionistUser = userRepository.findByEmail(receptionistEmail).orElseThrow();
        assert receptionistUser.getRole() == UserRole.RECEPTIONIST;
    }

    @Test
    void inviteReceptionist_duplicateShouldReturn400() throws Exception {
        mockMvc.perform(post("/clinics/{clinicId}/receptionists", clinicId)
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", receptionistEmail))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/clinics/{clinicId}/receptionists", clinicId)
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", receptionistEmail))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReceptionists_shouldReturnList() throws Exception {
        mockMvc.perform(post("/clinics/{clinicId}/receptionists", clinicId)
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", receptionistEmail))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/clinics/{clinicId}/receptionists", clinicId)
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value(receptionistEmail));
    }

    @Test
    void removeReceptionist_shouldReturn204AndRevertRole() throws Exception {
        String inviteResp = mockMvc.perform(post("/clinics/{clinicId}/receptionists", clinicId)
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", receptionistEmail))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String receptionistId = objectMapper.readTree(inviteResp).get("id").asText();

        mockMvc.perform(delete("/clinics/{clinicId}/receptionists/{receptionistId}", clinicId, receptionistId)
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());

        User receptionistUser = userRepository.findByEmail(receptionistEmail).orElseThrow();
        assert receptionistUser.getRole() == UserRole.PATIENT;
    }

    @Test
    void inviteReceptionist_nonOwnerShouldReturn400() throws Exception {
        CreateUserDTO otherDTO = CreateUserDTO.builder()
                .name("Other Prof").email("recep.other.unique@test.com")
                .password("other1234").cpf("63312300002")
                .phone("11900000063").birthDate(LocalDate.of(1985, 3, 20)).gender(Gender.MALE).build();

        String otherResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String otherId = objectMapper.readTree(otherResp).get("id").asText();
        User otherUser = userRepository.findById(otherId).orElseThrow();
        otherUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(otherUser);

        String otherToken = loginAndGetToken("recep.other.unique@test.com", "other1234");

        mockMvc.perform(post("/clinics/{clinicId}/receptionists", clinicId)
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", receptionistEmail))))
                .andExpect(status().isBadRequest());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequestDTO loginDTO = LoginRequestDTO.builder()
                .email(email).password(password).build();

        String resp = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(resp).get("token").asText();
    }
}
