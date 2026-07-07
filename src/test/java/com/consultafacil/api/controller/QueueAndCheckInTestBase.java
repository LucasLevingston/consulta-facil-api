package com.consultafacil.api.controller;

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
import com.consultafacil.ConsultaFacilApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * Base compartilhada dos testes de fila/check-in de appointments.
 * Concentra setup e helpers comuns; sem métodos @Test (ver subclasses).
 */
@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
abstract class QueueAndCheckInTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProfessionalProfileRepository professionalProfileRepository;
    @Autowired protected PatientProfileRepository patientProfileRepository;
    @Autowired protected AppointmentRepository appointmentRepository;
    @Value("${jwt.secret}")
    protected String jwtSecret;
    protected String patientToken;
    protected String professionalToken;
    protected String patientUserId;
    protected String professionalUserId;
    protected String professionalProfileId;
    protected String patientProfileId;

    @BeforeEach
    void setUp() throws Exception {
        String patientResp = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateUserDTO.builder().name("Queue Patient")
                                .email("queue.patient@test.com").password("password1").cpf("11122233344")
                                .phone("11911111111").birthDate(LocalDate.of(1990, 1, 1)).gender(Gender.FEMALE).build())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        patientUserId = objectMapper.readTree(patientResp).get("id").asText();
        patientToken = loginToken("queue.patient@test.com", "password1");
        patientProfileId = patientProfileRepository.findByUserId(patientUserId).orElseThrow().getId();
        String profResp = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateUserDTO.builder().name("Queue Doctor")
                                .email("queue.doctor@test.com").password("password1").cpf("55566677788")
                                .phone("11922222222").birthDate(LocalDate.of(1975, 6, 15)).gender(Gender.MALE).build())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        professionalUserId = objectMapper.readTree(profResp).get("id").asText();
        User profUser = userRepository.findById(professionalUserId).orElseThrow();
        profUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(profUser);
        ProfessionalProfile profile = ProfessionalProfile.builder().user(profUser)
                .specialty(Specialty.NEUROLOGIA).licenseNumber("CRM-RJ-99999").build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(profile).getId();
        professionalToken = loginToken("queue.doctor@test.com", "password1");
    }

    protected String loginToken(String email, String password) throws Exception {
        String resp = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequestDTO.builder().email(email).password(password).build())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }

    protected Appointment createAppointment(AppointmentStatus status) {
        PatientProfile patient = patientProfileRepository.findById(patientProfileId).orElseThrow();
        ProfessionalProfile professional = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        Appointment appt = Appointment.builder().patient(patient).professional(professional)
                .scheduledAt(LocalDateTime.now()).status(status).modality(AppointmentModality.IN_PERSON)
                .reason("Dor de cabeça").build();
        return appointmentRepository.saveAndFlush(appt);
    }
}
