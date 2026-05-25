package com.example.consulta.api.controller;

import com.example.consulta.api.dto.appointment.SaveAnamneseDTO;
import com.example.consulta.api.dto.appointment.SaveProntuarioDTO;
import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.AppointmentModality;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.PatientProfileRepository;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class AnamneseAndProntuarioIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PatientProfileRepository patientProfileRepository;
    @Autowired private ProfessionalProfileRepository professionalProfileRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    private String patientToken;
    private String professionalToken;
    private String patientUserId;
    private String professionalUserId;
    private String patientProfileId;
    private String professionalProfileId;

    @BeforeEach
    void setUp() throws Exception {
        String patientResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Anamnese Patient")
                        .email("anamnese.patient@test.com")
                        .password("password1")
                        .cpf("11122233344")
                        .phone("11911111111")
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .gender(Gender.FEMALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        patientUserId = objectMapper.readTree(patientResp).get("id").asText();
        patientToken = loginToken("anamnese.patient@test.com", "password1");

        PatientProfile patientProfile = patientProfileRepository.findByUserId(patientUserId).orElseThrow();
        patientProfileId = patientProfile.getId();

        String profResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Anamnese Doctor")
                        .email("anamnese.doctor@test.com")
                        .password("password1")
                        .cpf("55566677788")
                        .phone("11922222222")
                        .birthDate(LocalDate.of(1975, 6, 15))
                        .gender(Gender.MALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        professionalUserId = objectMapper.readTree(profResp).get("id").asText();

        User profUser = userRepository.findById(professionalUserId).orElseThrow();
        profUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(profUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(profUser).specialty("Clínica Geral").licenseNumber("CRM-SP-88888").build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(profile).getId();

        professionalToken = loginToken("anamnese.doctor@test.com", "password1");
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

    private Appointment createAppointment() {
        PatientProfile patient = patientProfileRepository.findById(patientProfileId).orElseThrow();
        ProfessionalProfile professional = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        return appointmentRepository.saveAndFlush(Appointment.builder()
                .patient(patient)
                .professional(professional)
                .scheduledAt(LocalDateTime.now())
                .status(AppointmentStatus.IN_PROGRESS)
                .modality(AppointmentModality.IN_PERSON)
                .reason("Check-up geral")
                .build());
    }

    // ─── GET /appointments/{id}/anamnesis ─────────────────────────────────────

    @Test
    void testGetAnamnesisNoContentWhenNotFilled() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(get("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetAnamnesisAfterSaveReturnsData() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveAnamneseDTO.builder()
                        .chiefComplaint("Dor de cabeça")
                        .allergies("Dipirona")
                        .build())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiefComplaint", equalTo("Dor de cabeça")))
                .andExpect(jsonPath("$.allergies", equalTo("Dipirona")));
    }

    // ─── PUT /appointments/{id}/anamnesis ─────────────────────────────────────

    @Test
    void testSaveAnamnesisAsPatientSuccess() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveAnamneseDTO.builder()
                        .chiefComplaint("Tontura")
                        .currentMedications("Losartana 50mg")
                        .allergies("Nenhuma")
                        .medicalHistory("Hipertensão")
                        .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiefComplaint", equalTo("Tontura")))
                .andExpect(jsonPath("$.currentMedications", equalTo("Losartana 50mg")));
    }

    @Test
    void testSaveAnamnesisAsProfessionalSuccess() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveAnamneseDTO.builder()
                        .chiefComplaint("Anotado pelo médico")
                        .observations("Paciente apresenta bom estado geral")
                        .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiefComplaint", equalTo("Anotado pelo médico")));
    }

    @Test
    void testSaveAnamnesisIdempotentUpdate() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveAnamneseDTO.builder()
                        .chiefComplaint("Primeiro preenchimento").build())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/appointments/" + appt.getId() + "/anamnesis")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveAnamneseDTO.builder()
                        .chiefComplaint("Atualização").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiefComplaint", equalTo("Atualização")));
    }

    // ─── GET /appointments/{id}/prontuario ────────────────────────────────────

    @Test
    void testGetProntuarioNoContentWhenNotFilled() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(get("/appointments/" + appt.getId() + "/prontuario")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetProntuarioAfterSaveReturnsData() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/prontuario")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveProntuarioDTO.builder()
                        .clinicalNotes("Paciente estável")
                        .diagnosis("Hipertensão arterial")
                        .diagnosisCid("I10")
                        .build())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/appointments/" + appt.getId() + "/prontuario")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis", equalTo("Hipertensão arterial")))
                .andExpect(jsonPath("$.diagnosisCid", equalTo("I10")));
    }

    // ─── PUT /appointments/{id}/prontuario ────────────────────────────────────

    @Test
    void testSaveProntuarioAsProfessionalSuccess() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/prontuario")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveProntuarioDTO.builder()
                        .clinicalNotes("Paciente em bom estado geral")
                        .diagnosis("Cefaleia tensional")
                        .diagnosisCid("G44.2")
                        .prescription("Paracetamol 750mg 8/8h por 3 dias")
                        .treatmentPlan("Repouso e hidratação")
                        .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clinicalNotes", equalTo("Paciente em bom estado geral")))
                .andExpect(jsonPath("$.diagnosis", equalTo("Cefaleia tensional")))
                .andExpect(jsonPath("$.prescription", equalTo("Paracetamol 750mg 8/8h por 3 dias")));
    }

    @Test
    void testSaveProntuarioAsPatientFails() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/prontuario")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveProntuarioDTO.builder()
                        .clinicalNotes("Tentativa indevida")
                        .build())))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSaveProntuarioIdempotentUpdate() throws Exception {
        Appointment appt = createAppointment();

        mockMvc.perform(put("/appointments/" + appt.getId() + "/prontuario")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveProntuarioDTO.builder()
                        .clinicalNotes("Inicial").build())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/appointments/" + appt.getId() + "/prontuario")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SaveProntuarioDTO.builder()
                        .clinicalNotes("Atualizado").diagnosis("Nova hipótese").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clinicalNotes", equalTo("Atualizado")))
                .andExpect(jsonPath("$.diagnosis", equalTo("Nova hipótese")));
    }
}
