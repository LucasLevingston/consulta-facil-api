package com.consultafacil.api.controller;

import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.ClinicMember;
import com.consultafacil.domain.entity.ClinicMemberId;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.AppointmentRepository;
import com.consultafacil.domain.repository.ClinicMemberRepository;
import com.consultafacil.domain.repository.ClinicRepository;
import com.consultafacil.domain.repository.PatientProfileRepository;
import com.consultafacil.domain.repository.ProfessionalProfileRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class ClinicQueueIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PatientProfileRepository patientProfileRepository;
    @Autowired private ProfessionalProfileRepository professionalProfileRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private ClinicRepository clinicRepository;
    @Autowired private ClinicMemberRepository clinicMemberRepository;

    private String patientProfileId;
    private String professionalProfileId;
    private String clinicId;
    private String ownerToken;

    @BeforeEach
    void setUp() throws Exception {
        String patientResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Queue Patient")
                        .email("cq.patient@test.com")
                        .password("password1")
                        .cpf("11122233344")
                        .phone("11911111111")
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .gender(Gender.FEMALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String patientUserId = objectMapper.readTree(patientResp).get("id").asText();
        patientProfileId = patientProfileRepository.findByUserId(patientUserId).orElseThrow().getId();

        String profResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Clinic Doctor")
                        .email("cq.doctor@test.com")
                        .password("password1")
                        .cpf("55566677788")
                        .phone("11922222222")
                        .birthDate(LocalDate.of(1975, 6, 15))
                        .gender(Gender.MALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String profUserId = objectMapper.readTree(profResp).get("id").asText();

        User profUser = userRepository.findById(profUserId).orElseThrow();
        profUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(profUser);

        ProfessionalProfile profile = professionalProfileRepository.saveAndFlush(
                ProfessionalProfile.builder().user(profUser).specialty("Neurologia").licenseNumber("CRM-RJ-77777").build());
        professionalProfileId = profile.getId();

        String ownerResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Clinic Owner")
                        .email("cq.owner@test.com")
                        .password("password1")
                        .cpf("99988877766")
                        .phone("11933333333")
                        .birthDate(LocalDate.of(1970, 3, 20))
                        .gender(Gender.MALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ownerUserId = objectMapper.readTree(ownerResp).get("id").asText();

        User ownerUser = userRepository.findById(ownerUserId).orElseThrow();
        Clinic clinic = clinicRepository.saveAndFlush(
                Clinic.builder().name("Test Clinic").owner(ownerUser).build());
        clinicId = clinic.getId();
        ownerToken = loginToken("cq.owner@test.com", "password1");

        clinicMemberRepository.saveAndFlush(ClinicMember.builder()
                .id(new ClinicMemberId(clinicId, professionalProfileId))
                .clinic(clinic)
                .professionalProfile(profile)
                .build());
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

    private Appointment createAppointment(AppointmentStatus status) {
        PatientProfile patient = patientProfileRepository.findById(patientProfileId).orElseThrow();
        ProfessionalProfile professional = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        return appointmentRepository.saveAndFlush(Appointment.builder()
                .patient(patient)
                .professional(professional)
                .scheduledAt(LocalDateTime.now())
                .status(status)
                .modality(AppointmentModality.IN_PERSON)
                .reason("Consulta")
                .build());
    }

    @Test
    void testGetClinicQueueReturnsTodaysCheckedInAppointments() throws Exception {
        createAppointment(AppointmentStatus.CHECKED_IN);
        createAppointment(AppointmentStatus.IN_PROGRESS);
        createAppointment(AppointmentStatus.CONFIRMED); // should NOT appear

        mockMvc.perform(get("/clinics/" + clinicId + "/queue")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder("CHECKED_IN", "IN_PROGRESS")));
    }

    @Test
    void testGetClinicQueueEmptyWhenNoneCheckedIn() throws Exception {
        createAppointment(AppointmentStatus.CONFIRMED);

        mockMvc.perform(get("/clinics/" + clinicId + "/queue")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetClinicQueueEmptyForUnknownClinic() throws Exception {
        mockMvc.perform(get("/clinics/nonexistent-clinic-id/queue")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
