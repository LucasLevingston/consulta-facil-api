package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.exam.CreateExamRequestDTO;
import com.consultafacil.api.dto.exam.ReviewExamRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.ExamRequest;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.examrequest.ExamRequestRepository;
import com.consultafacil.domain.repository.professional.profile.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.user.UserRepository;
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
import com.consultafacil.domain.enums.ExamType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.ExamType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.ExamType;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class ExamRequestControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfessionalProfileRepository professionalProfileRepository;
    @Autowired private ExamRequestRepository examRequestRepository;

    private String patientToken;
    private String professionalToken;
    private String professionalProfileId;
    private String appointmentId;

    @BeforeEach
    void setUp() throws Exception {
        // Register patient
        CreateUserDTO patientDTO = CreateUserDTO.builder()
                .name("Exam Patient Unique")
                .email("examrequest.patient.unique@test.com")
                .password("patient123")
                .cpf("47712300000")
                .phone("11977770001")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.FEMALE)
                .build();
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated());

        String patientLoginResp = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("examrequest.patient.unique@test.com").password("patient123").build())))
                .andReturn().getResponse().getContentAsString();
        patientToken = objectMapper.readTree(patientLoginResp).get("token").asText();

        // Register professional
        CreateUserDTO profDTO = CreateUserDTO.builder()
                .name("Exam Professional Unique")
                .email("examrequest.prof.unique@test.com")
                .password("prof1234")
                .cpf("47712300001")
                .phone("11977770002")
                .birthDate(LocalDate.of(1975, 6, 15))
                .gender(Gender.MALE)
                .build();
        String profRegResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String profUserId = objectMapper.readTree(profRegResp).get("id").asText();

        User profUser = userRepository.findById(profUserId).orElseThrow();
        profUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(profUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(profUser)
                .specialty(Specialty.ORTOPEDIA)
                .licenseNumber("CRM-SP-47700")
                .build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(profile).getId();

        String profLoginResp = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("examrequest.prof.unique@test.com").password("prof1234").build())))
                .andReturn().getResponse().getContentAsString();
        professionalToken = objectMapper.readTree(profLoginResp).get("token").asText();

        // Create an appointment
        CreateAppointmentDTO apptDTO = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(5))
                .reason("Consulta ortopédica")
                .build();
        String apptResp = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(apptDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        appointmentId = objectMapper.readTree(apptResp).get("id").asText();
    }

    @Test
    void testRequestExam() throws Exception {
        CreateExamRequestDTO dto = new CreateExamRequestDTO();
        dto.setExamName(ExamType.RAIO_X);
        dto.setInstructions("Em jejum por 2 horas");

        mockMvc.perform(post("/appointments/" + appointmentId + "/exams")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.examName", equalTo("RAIO_X")))
                .andExpect(jsonPath("$.status", equalTo("PENDING")))
                .andExpect(jsonPath("$.instructions", equalTo("Em jejum por 2 horas")));
    }

    @Test
    void testGetExamsByAppointment() throws Exception {
        CreateExamRequestDTO dto = new CreateExamRequestDTO();
        dto.setExamName(ExamType.HEMOGRAMA_COMPLETO);

        mockMvc.perform(post("/appointments/" + appointmentId + "/exams")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/appointments/" + appointmentId + "/exams")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].examName", equalTo("HEMOGRAMA_COMPLETO")));
    }

    @Test
    void testPatientCannotRequestExam() throws Exception {
        CreateExamRequestDTO dto = new CreateExamRequestDTO();
        dto.setExamName(ExamType.RAIO_X);

        mockMvc.perform(post("/appointments/" + appointmentId + "/exams")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testReviewExamRequiresUploadedStatus() throws Exception {
        CreateExamRequestDTO createDTO = new CreateExamRequestDTO();
        createDTO.setExamName(ExamType.RESSONANCIA_MAGNETICA);
        String createResp = mockMvc.perform(post("/appointments/" + appointmentId + "/exams")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String examId = objectMapper.readTree(createResp).get("id").asText();

        ReviewExamRequestDTO reviewDTO = new ReviewExamRequestDTO();
        reviewDTO.setProfessionalNotes("Resultado normal.");

        // Status is PENDING (not UPLOADED) — should fail
        mockMvc.perform(put("/exams/" + examId + "/review")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testReviewExamAfterUpload() throws Exception {
        CreateExamRequestDTO createDTO = new CreateExamRequestDTO();
        createDTO.setExamName(ExamType.ULTRASSONOGRAFIA);
        String createResp = mockMvc.perform(post("/appointments/" + appointmentId + "/exams")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String examId = objectMapper.readTree(createResp).get("id").asText();

        // Set status to UPLOADED via repository directly (S3 unavailable in tests)
        ExamRequest examRequest = examRequestRepository.findById(examId).orElseThrow();
        examRequest.setStatus(ExamRequestStatus.UPLOADED);
        examRequest.setFileUrl("https://s3.example.com/exams/test.pdf");
        examRequest.setFileName("resultado.pdf");
        examRequestRepository.saveAndFlush(examRequest);

        ReviewExamRequestDTO reviewDTO = new ReviewExamRequestDTO();
        reviewDTO.setProfessionalNotes("Resultado dentro do esperado.");

        mockMvc.perform(put("/exams/" + examId + "/review")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("REVIEWED")))
                .andExpect(jsonPath("$.professionalNotes", equalTo("Resultado dentro do esperado.")));
    }
}
