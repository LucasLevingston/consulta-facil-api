package com.consultafacil.api.controller;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.appointment.AppointmentRepository;
import com.consultafacil.domain.repository.patient.PatientProfileRepository;
import com.consultafacil.domain.repository.professional.profile.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
abstract class MedicalHistoryTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected PatientProfileRepository patientProfileRepository;
    @Autowired protected ProfessionalProfileRepository professionalProfileRepository;
    @Autowired protected AppointmentRepository appointmentRepository;

    protected String patientToken, professionalToken, patientProfileId, professionalProfileId;

    @BeforeEach
    void setUp() throws Exception {
        String patientResp = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("MedicalHistory Patient").email("medicalHistory.patient@test.com").password("password1")
                        .cpf("11122233344").phone("11911111111")
                        .birthDate(LocalDate.of(1990, 1, 1)).gender(Gender.FEMALE).build())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String patientUserId = objectMapper.readTree(patientResp).get("id").asText();
        patientToken = loginToken("medicalHistory.patient@test.com", "password1");
        patientProfileId = patientProfileRepository.findByUserId(patientUserId).orElseThrow().getId();

        String profResp = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("MedicalHistory Doctor").email("medicalHistory.doctor@test.com").password("password1")
                        .cpf("55566677788").phone("11922222222")
                        .birthDate(LocalDate.of(1975, 6, 15)).gender(Gender.MALE).build())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String professionalUserId = objectMapper.readTree(profResp).get("id").asText();

        User profUser = userRepository.findById(professionalUserId).orElseThrow();
        profUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(profUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(profUser).specialty(Specialty.CLINICA_GERAL).licenseNumber("CRM-SP-88888").build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(profile).getId();
        professionalToken = loginToken("medicalHistory.doctor@test.com", "password1");
    }

    protected String loginToken(String email, String password) throws Exception {
        String resp = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder().email(email).password(password).build())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }

    protected Appointment createAppointment() {
        PatientProfile patient = patientProfileRepository.findById(patientProfileId).orElseThrow();
        ProfessionalProfile professional = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        return appointmentRepository.saveAndFlush(Appointment.builder()
                .patient(patient).professional(professional).scheduledAt(LocalDateTime.now())
                .status(AppointmentStatus.IN_PROGRESS).modality(AppointmentModality.IN_PERSON)
                .reason("Check-up geral").build());
    }
}
