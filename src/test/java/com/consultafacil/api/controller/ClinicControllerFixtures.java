package com.consultafacil.api.controller;

import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.professional.profile.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Shared MockMvc fixtures for clinic/professional integration tests. Not a test class itself. */
abstract class ClinicControllerFixtures {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProfessionalProfileRepository professionalProfileRepository;

    protected String registerUser(String name, String email, String password, String cpf,
                                   String phone, LocalDate birthDate, Gender gender) throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password).cpf(cpf).phone(phone)
                .birthDate(birthDate).gender(gender).build();
        String resp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }

    protected void promoteRole(String userId, UserRole role) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(role);
        userRepository.saveAndFlush(user);
    }

    protected String createDoctorProfile(String userId, Specialty specialty, String license) {
        User user = userRepository.findById(userId).orElseThrow();
        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user).specialty(specialty)
                .licenseNumber(license).status(ProfessionalProfileStatus.ACTIVE).build();
        return professionalProfileRepository.saveAndFlush(profile).getId();
    }

    protected String loginAndGetToken(String email, String password) throws Exception {
        String resp = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder().email(email).password(password).build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }

    protected String createClinicAndGetId(String ownerToken, String name) throws Exception {
        Map<String, Object> body = Map.of(
                "name", name,
                "description", "Clínica especializada",
                "phone", "1133334444",
                "address", "Rua das Flores, 100",
                "city", "São Paulo",
                "state", "SP",
                "zipCode", "01310100"
        );
        String resp = mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }
}
