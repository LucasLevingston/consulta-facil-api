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
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.domain.repository.appointment.AppointmentRepository;
import com.consultafacil.domain.repository.patient.PatientProfileRepository;
import com.consultafacil.domain.repository.professional.profile.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.user.UserRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
abstract class PrescriptionControllerTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProfessionalProfileRepository professionalProfileRepository;
    @Autowired protected PatientProfileRepository patientProfileRepository;
    @Autowired protected AppointmentRepository appointmentRepository;

    @MockitoBean
    protected StoragePort storagePort;

    @MockitoBean
    protected EmailPort emailPort;

    protected String professionalToken;
    protected String patientToken;
    protected String professionalUserId;
    protected String patientUserId;
    protected String appointmentId;

    @BeforeEach
    void setUp() throws Exception {
        // Mock storage upload — returns a fake URL
        when(storagePort.upload(any(), anyString(), anyString(), anyString()))
                .thenReturn("https://s3.example.com/prescriptions/test.pdf");
        doNothing().when(emailPort).sendPrescriptionAvailable(anyString(), anyString(), anyString());

        // Register professional user
        professionalUserId = registerUser(
                "Prescription Prof", "presc-prof@example.com", "prescprof1",
                "51122233344", "11999991111");
        promoteTo(professionalUserId, UserRole.PROFESSIONAL);
        ProfessionalProfile profProfile = createProfessionalProfile(professionalUserId);
        professionalToken = loginAndGetToken("presc-prof@example.com", "prescprof1");

        // Register patient user
        patientUserId = registerUser(
                "Prescription Patient", "presc-patient@example.com", "prescpat1",
                "55566677711", "11999992222");
        User patientUser = userRepository.findById(patientUserId).orElseThrow();
        PatientProfile patientProfile = patientProfileRepository.findByUserId(patientUserId)
                .orElseGet(() -> patientProfileRepository.saveAndFlush(
                        PatientProfile.builder().user(patientUser).build()));
        patientToken = loginAndGetToken("presc-patient@example.com", "prescpat1");

        // Create an appointment within 48h (scheduled 1h ago — still within the 48h window)
        Appointment appointment = Appointment.builder()
                .patient(patientProfile)
                .professional(profProfile)
                .scheduledAt(LocalDateTime.now().minusHours(1))
                .reason("Test consultation")
                .modality(AppointmentModality.IN_PERSON)
                .status(AppointmentStatus.CONFIRMED)
                .build();
        appointmentId = appointmentRepository.saveAndFlush(appointment).getId();
    }

    private String registerUser(String name, String email, String password,
                                String cpf, String phone) throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password).cpf(cpf)
                .phone(phone).birthDate(LocalDate.of(1980, 4, 12)).gender(Gender.MALE)
                .build();
        String resp = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }

    private void promoteTo(String userId, UserRole role) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(role);
        userRepository.saveAndFlush(user);
    }

    private ProfessionalProfile createProfessionalProfile(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user)
                .specialty(Specialty.CARDIOLOGIA)
                .licenseNumber("CRM-SP-77001")
                .status(ProfessionalProfileStatus.ACTIVE)
                .build();
        return professionalProfileRepository.saveAndFlush(profile);
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
}
