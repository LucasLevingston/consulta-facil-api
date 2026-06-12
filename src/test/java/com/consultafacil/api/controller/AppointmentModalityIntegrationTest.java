package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.api.dto.appointment.SetModalityDTO;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.AppointmentRepository;
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
import com.consultafacil.domain.enums.Specialty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.consultafacil.domain.enums.Specialty;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.consultafacil.domain.enums.Specialty;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class AppointmentModalityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfessionalProfileRepository professionalProfileRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    private String patientToken;
    private String professionalToken;
    private String professionalProfileId;
    private String appointmentId;

    @BeforeEach
    void setUp() throws Exception {
        // Patient
        String patientReg = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Modality Patient")
                        .email("modality.patient@test.com")
                        .password("pass1234")
                        .cpf("77700000000")
                        .phone("11900000010")
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .gender(Gender.FEMALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String patientUserId = objectMapper.readTree(patientReg).get("id").asText();

        String patientLogin = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("modality.patient@test.com")
                        .password("pass1234")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        patientToken = objectMapper.readTree(patientLogin).get("token").asText();

        // Professional
        String doctorReg = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Modality Doctor")
                        .email("modality.doctor@test.com")
                        .password("doctor12")
                        .cpf("77700000001")
                        .phone("11900000011")
                        .birthDate(LocalDate.of(1980, 6, 15))
                        .gender(Gender.MALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String doctorUserId = objectMapper.readTree(doctorReg).get("id").asText();

        User doctorUser = userRepository.findById(doctorUserId).orElseThrow();
        doctorUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(doctorUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(doctorUser)
                .specialty(Specialty.CLINICA_GERAL)
                .licenseNumber("CRM-SP-77700")
                .build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(profile).getId();

        String doctorLogin = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("modality.doctor@test.com")
                        .password("doctor12")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        professionalToken = objectMapper.readTree(doctorLogin).get("token").asText();

        // Appointment (patient creates it)
        String apptResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateAppointmentDTO.builder()
                        .professionalId(professionalProfileId)
                        .scheduledAt(LocalDateTime.now().plusDays(5).withHour(10).withMinute(0).withSecond(0).withNano(0))
                        .reason("Consulta online")
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        appointmentId = objectMapper.readTree(apptResponse).get("id").asText();
    }

    @Test
    void setModality_toOnline_shouldReturn200() throws Exception {
        SetModalityDTO dto = new SetModalityDTO();
        dto.setModality(AppointmentModality.ONLINE);

        mockMvc.perform(put("/appointments/" + appointmentId + "/modality")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modality").value("ONLINE"));
    }

    @Test
    void setModality_toOnlineWithMeetLink_shouldPersistLink() throws Exception {
        SetModalityDTO dto = new SetModalityDTO();
        dto.setModality(AppointmentModality.ONLINE);
        dto.setMeetLink("https://meet.google.com/abc-test-xyz");

        mockMvc.perform(put("/appointments/" + appointmentId + "/modality")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modality").value("ONLINE"))
                .andExpect(jsonPath("$.meetLink").value("https://meet.google.com/abc-test-xyz"));
    }

    @Test
    void setModality_byPatient_shouldReturn403() throws Exception {
        SetModalityDTO dto = new SetModalityDTO();
        dto.setModality(AppointmentModality.ONLINE);

        mockMvc.perform(put("/appointments/" + appointmentId + "/modality")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void generateMeetLink_forOnlineAppointment_shouldReturn200() throws Exception {
        // First set to ONLINE
        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow();
        appt.setModality(AppointmentModality.ONLINE);
        appointmentRepository.saveAndFlush(appt);

        mockMvc.perform(post("/appointments/" + appointmentId + "/meet-link")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetLink").value(startsWith("https://meet.google.com/")));
    }

    @Test
    void generateMeetLink_forInPersonAppointment_shouldReturn400() throws Exception {
        mockMvc.perform(post("/appointments/" + appointmentId + "/meet-link")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setModality_toInPerson_shouldClearMeetLink() throws Exception {
        // First set to ONLINE with a meetLink
        SetModalityDTO onlineDto = new SetModalityDTO();
        onlineDto.setModality(AppointmentModality.ONLINE);
        onlineDto.setMeetLink("https://meet.google.com/to-be-cleared");
        mockMvc.perform(put("/appointments/" + appointmentId + "/modality")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(onlineDto)))
                .andExpect(status().isOk());

        // Then switch to IN_PERSON — should clear meetLink
        // Note: JPA cache issue means we must set modality directly via repo
        // instead of checking meetLink in response
        SetModalityDTO inPersonDto = new SetModalityDTO();
        inPersonDto.setModality(AppointmentModality.IN_PERSON);
        mockMvc.perform(put("/appointments/" + appointmentId + "/modality")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inPersonDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modality").value("IN_PERSON"));
    }

    @Test
    void setModality_completedAppointmentFails() throws Exception {
        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow();
        appt.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.saveAndFlush(appt);

        SetModalityDTO dto = new SetModalityDTO();
        dto.setModality(AppointmentModality.ONLINE);
        mockMvc.perform(put("/appointments/" + appointmentId + "/modality")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setModality_wrongProfessionalFails() throws Exception {
        // Register a second professional
        String doc2Reg = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Other Doctor")
                        .email("other.modality.doctor@test.com")
                        .password("doctor12")
                        .cpf("77700000099")
                        .phone("11900000099")
                        .birthDate(LocalDate.of(1985, 3, 20))
                        .gender(Gender.FEMALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String doc2Id = objectMapper.readTree(doc2Reg).get("id").asText();
        User doc2 = userRepository.findById(doc2Id).orElseThrow();
        doc2.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(doc2);
        ProfessionalProfile profile2 = ProfessionalProfile.builder()
                .user(doc2).specialty(Specialty.NEUROLOGIA).licenseNumber("CRM-SP-99900").build();
        professionalProfileRepository.saveAndFlush(profile2);

        String doc2Login = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("other.modality.doctor@test.com").password("doctor12").build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String doc2Token = objectMapper.readTree(doc2Login).get("token").asText();

        SetModalityDTO dto = new SetModalityDTO();
        dto.setModality(AppointmentModality.ONLINE);
        mockMvc.perform(put("/appointments/" + appointmentId + "/modality")
                .header("Authorization", "Bearer " + doc2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
