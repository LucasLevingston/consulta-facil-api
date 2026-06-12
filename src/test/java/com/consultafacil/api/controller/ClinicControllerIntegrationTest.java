package com.consultafacil.api.controller;

import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.UserRole;
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
import java.util.Map;
import com.consultafacil.domain.enums.Specialty;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.consultafacil.domain.enums.Specialty;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class ClinicControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfessionalProfileRepository professionalProfileRepository;

    private String doctorToken;
    private String doctorUserId;
    private String professionalProfileId;
    private String adminToken;
    private String secondDoctorToken;
    private String secondProfessionalProfileId;

    @BeforeEach
    void setUp() throws Exception {
        // Register + promote doctor
        CreateUserDTO doctorDTO = CreateUserDTO.builder()
                .name("Dr. João").email("joao@example.com").password("senha123")
                .cpf("11122233344").phone("11900000001")
                .birthDate(LocalDate.of(1980, 1, 1)).gender(Gender.MALE).build();

        String regResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        doctorUserId = objectMapper.readTree(regResp).get("id").asText();
        User doctorUser = userRepository.findById(doctorUserId).orElseThrow();
        doctorUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(doctorUser);

        ProfessionalProfile dp = ProfessionalProfile.builder()
                .user(doctorUser).specialty(Specialty.CARDIOLOGIA)
                .licenseNumber("CRM-SP-11111").status(ProfessionalProfileStatus.ACTIVE).build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(dp).getId();

        doctorToken = loginAndGetToken("joao@example.com", "senha123");

        // Register admin
        CreateUserDTO adminDTO = CreateUserDTO.builder()
                .name("Admin").email("admin@clinic.com").password("admin123")
                .cpf("99988877766").phone("11900000099")
                .birthDate(LocalDate.of(1975, 6, 15)).gender(Gender.FEMALE).build();

        String adminResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String adminId = objectMapper.readTree(adminResp).get("id").asText();
        User adminUser = userRepository.findById(adminId).orElseThrow();
        adminUser.setRole(UserRole.ADMIN);
        userRepository.saveAndFlush(adminUser);
        adminToken = loginAndGetToken("admin@clinic.com", "admin123");

        // Register second doctor for member tests
        CreateUserDTO secondDTO = CreateUserDTO.builder()
                .name("Dr. Maria").email("maria@example.com").password("senha456")
                .cpf("55566677788").phone("11900000002")
                .birthDate(LocalDate.of(1985, 5, 10)).gender(Gender.FEMALE).build();

        String secondResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String secondId = objectMapper.readTree(secondResp).get("id").asText();
        User secondUser = userRepository.findById(secondId).orElseThrow();
        secondUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(secondUser);

        ProfessionalProfile dp2 = ProfessionalProfile.builder()
                .user(secondUser).specialty(Specialty.NEUROLOGIA)
                .licenseNumber("CRM-SP-22222").status(ProfessionalProfileStatus.ACTIVE).build();
        secondProfessionalProfileId = professionalProfileRepository.saveAndFlush(dp2).getId();
        secondDoctorToken = loginAndGetToken("maria@example.com", "senha456");
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String resp = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder().email(email).password(password).build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }

    private String createClinicAndGetId(String ownerToken, String name) throws Exception {
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

    // ─── GET /clinics (public) ─────────────────────────────────────────────────

    @Test
    void testListClinics_public_returnsEmptyWhenNone() throws Exception {
        mockMvc.perform(get("/clinics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", instanceOf(java.util.ArrayList.class)));
    }

    @Test
    void testListClinics_public_returnsClinics() throws Exception {
        createClinicAndGetId(doctorToken, "Clínica A");

        mockMvc.perform(get("/clinics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", equalTo("Clínica A")))
                .andExpect(jsonPath("$[0].ownerId", equalTo(doctorUserId)));
    }

    // ─── GET /clinics/{id} (public) ──────────────────────────────────────────

    @Test
    void testGetClinicById_returnsClinic() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Cardio Center");

        mockMvc.perform(get("/clinics/" + clinicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(clinicId)))
                .andExpect(jsonPath("$.name", equalTo("Cardio Center")))
                .andExpect(jsonPath("$.city", equalTo("São Paulo")));
    }

    @Test
    void testGetClinicById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/clinics/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /clinics/my (requires DOCTOR) ────────────────────────────────────

    @Test
    void testGetMyClinic_returnsEmptyWhenNone() throws Exception {
        mockMvc.perform(get("/clinics/my")
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetMyClinic_returnsOwnClinic() throws Exception {
        createClinicAndGetId(doctorToken, "Minha Clínica");

        mockMvc.perform(get("/clinics/my")
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo("Minha Clínica")))
                .andExpect(jsonPath("$[0].members[0].role", equalTo("OWNER")));
    }

    @Test
    void testGetMyClinic_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/clinics/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetMyClinic_adminCanAccess() throws Exception {
        mockMvc.perform(get("/clinics/my")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetMyClinic_patientCannotAccess() throws Exception {
        CreateUserDTO patientDTO = CreateUserDTO.builder()
                .name("Patient").email("patient@example.com").password("pass1234")
                .cpf("33344455566").phone("11900000003")
                .birthDate(LocalDate.of(1995, 3, 20)).gender(Gender.MALE).build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated());

        String patientToken = loginAndGetToken("patient@example.com", "pass1234");

        mockMvc.perform(get("/clinics/my")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    // ─── POST /clinics (requires DOCTOR) ─────────────────────────────────────

    @Test
    void testCreateClinic_success() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Cardio Clinic",
                "description", "Especializada em coração",
                "phone", "1122223333",
                "city", "São Paulo",
                "state", "SP"
        );

        mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", equalTo("Cardio Clinic")))
                .andExpect(jsonPath("$.city", equalTo("São Paulo")))
                .andExpect(jsonPath("$.status", equalTo("ACTIVE")))
                .andExpect(jsonPath("$.ownerId", equalTo(doctorUserId)))
                .andExpect(jsonPath("$.members", hasSize(1)))
                .andExpect(jsonPath("$.members[0].role", equalTo("OWNER")));
    }

    @Test
    void testCreateClinic_withCoordinates_savesLocation() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Geo Clinic",
                "city", "São Paulo",
                "latitude", -23.5505,
                "longitude", -46.6333
        );

        mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.latitude", closeTo(-23.5505, 0.001)))
                .andExpect(jsonPath("$.longitude", closeTo(-46.6333, 0.001)));
    }

    @Test
    void testCreateClinic_missingName_returns400() throws Exception {
        Map<String, Object> body = Map.of("city", "São Paulo");

        mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateClinic_requiresAuthentication() throws Exception {
        Map<String, Object> body = Map.of("name", "Anon Clinic");

        mockMvc.perform(post("/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    // ─── PUT /clinics/{id} ────────────────────────────────────────────────────

    @Test
    void testUpdateClinic_ownerCanUpdate() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Original Name");

        Map<String, Object> update = Map.of(
                "name", "Updated Name",
                "city", "Rio de Janeiro",
                "state", "RJ"
        );

        mockMvc.perform(put("/clinics/" + clinicId)
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Updated Name")))
                .andExpect(jsonPath("$.city", equalTo("Rio de Janeiro")));
    }

    @Test
    void testUpdateClinic_nonOwnerCannotUpdate() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Locked Clinic");

        Map<String, Object> update = Map.of("name", "Hacked Name", "city", "Hack City");

        mockMvc.perform(put("/clinics/" + clinicId)
                .header("Authorization", "Bearer " + secondDoctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateClinic_withImageUrl() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Image Clinic");

        Map<String, Object> update = new java.util.HashMap<>();
        update.put("name", "Image Clinic Updated");
        update.put("imageUrl", "https://example.com/logo.png");

        mockMvc.perform(put("/clinics/" + clinicId)
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Image Clinic Updated")));
    }

    // ─── Member management ────────────────────────────────────────────────────

    @Test
    void testAddMember_ownerAddsDoctor() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Multi Clinic");

        mockMvc.perform(post("/clinics/" + clinicId + "/members/" + secondProfessionalProfileId)
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clinics/" + clinicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members", hasSize(2)));
    }

    @Test
    void testAddMember_nonOwnerCannotAdd() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Protected Clinic");

        mockMvc.perform(post("/clinics/" + clinicId + "/members/" + secondProfessionalProfileId)
                .header("Authorization", "Bearer " + secondDoctorToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddMember_duplicateReturns400() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Dupe Test Clinic");

        // Owner already added as OWNER member on creation — adding again should fail
        mockMvc.perform(post("/clinics/" + clinicId + "/members/" + professionalProfileId)
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemoveMember_ownerRemovesDoctor() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Remove Test Clinic");

        // Add second doctor first
        mockMvc.perform(post("/clinics/" + clinicId + "/members/" + secondProfessionalProfileId)
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isNoContent());

        // Now remove
        mockMvc.perform(delete("/clinics/" + clinicId + "/members/" + secondProfessionalProfileId)
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clinics/" + clinicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members", hasSize(1)));
    }

    @Test
    void testRemoveMember_nonOwnerCannotRemove() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Non-Owner Remove Test");

        mockMvc.perform(post("/clinics/" + clinicId + "/members/" + secondProfessionalProfileId)
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/clinics/" + clinicId + "/members/" + secondProfessionalProfileId)
                .header("Authorization", "Bearer " + secondDoctorToken))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /clinics/nearby ─────────────────────────────────────────────────

    @Test
    void testGetClinicsNearby_returnsClinicWithinRadius() throws Exception {
        // São Paulo coordinates
        Map<String, Object> body = Map.of(
                "name", "Nearby Clinic",
                "city", "São Paulo",
                "latitude", -23.5505,
                "longitude", -46.6333
        );

        mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // Query from same location with 10km radius
        mockMvc.perform(get("/clinics/nearby")
                .param("lat", "-23.5505")
                .param("lng", "-46.6333")
                .param("radiusKm", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", equalTo("Nearby Clinic")));
    }

    @Test
    void testGetClinicsNearby_excludesFarClinics() throws Exception {
        // Clinic in São Paulo
        Map<String, Object> body = Map.of(
                "name", "SP Clinic", "city", "São Paulo",
                "latitude", -23.5505, "longitude", -46.6333
        );
        mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // Query from Manaus (3000km away) with 50km radius — should return nothing
        mockMvc.perform(get("/clinics/nearby")
                .param("lat", "-3.1316")
                .param("lng", "-60.0213")
                .param("radiusKm", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetClinicsNearby_clinicWithoutCoordinatesExcluded() throws Exception {
        // Clinic without coordinates
        Map<String, Object> body = Map.of("name", "No Location Clinic", "city", "São Paulo");
        mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/clinics/nearby")
                .param("lat", "-23.5505")
                .param("lng", "-46.6333")
                .param("radiusKm", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ─── GET /professionals/nearby ────────────────────────────────────────────

    @Test
    void testGetDoctorsNearby_returnsDoctorWithinRadius() throws Exception {
        ProfessionalProfile dp = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        dp.setLatitude(-23.5505);
        dp.setLongitude(-46.6333);
        professionalProfileRepository.saveAndFlush(dp);

        mockMvc.perform(get("/professionals/nearby")
                .param("lat", "-23.5505")
                .param("lng", "-46.6333")
                .param("radiusKm", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id", equalTo(professionalProfileId)));
    }

    @Test
    void testGetDoctorsNearby_excludesFarDoctors() throws Exception {
        ProfessionalProfile dp = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        dp.setLatitude(-23.5505);
        dp.setLongitude(-46.6333);
        professionalProfileRepository.saveAndFlush(dp);

        // Query from Manaus
        mockMvc.perform(get("/professionals/nearby")
                .param("lat", "-3.1316")
                .param("lng", "-60.0213")
                .param("radiusKm", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetDoctorsNearby_filtersBySpecialty() throws Exception {
        ProfessionalProfile dp1 = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        dp1.setLatitude(-23.5505);
        dp1.setLongitude(-46.6333);
        professionalProfileRepository.saveAndFlush(dp1);

        ProfessionalProfile dp2 = professionalProfileRepository.findById(secondProfessionalProfileId).orElseThrow();
        dp2.setLatitude(-23.5510);
        dp2.setLongitude(-46.6340);
        professionalProfileRepository.saveAndFlush(dp2);

        // Both are nearby, but filter by Cardiologia only gets dp1
        mockMvc.perform(get("/professionals/nearby")
                .param("lat", "-23.5505")
                .param("lng", "-46.6333")
                .param("radiusKm", "10")
                .param("specialty", "Cardiologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].specialty", equalTo("CARDIOLOGIA")));
    }

    @Test
    void testGetDoctorsNearby_excludesDoctorsWithoutCoordinates() throws Exception {
        // professionalProfileId has no coordinates set — should not appear
        mockMvc.perform(get("/professionals/nearby")
                .param("lat", "-23.5505")
                .param("lng", "-46.6333")
                .param("radiusKm", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetDoctorsNearby_isPublic() throws Exception {
        mockMvc.perform(get("/professionals/nearby")
                .param("lat", "-23.5505")
                .param("lng", "-46.6333"))
                .andExpect(status().isOk());
    }
}
