package com.consultafacil.api.controller;

import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class ClinicWorkingHoursIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessionalProfileRepository professionalProfileRepository;

    private String ownerToken;
    private String otherToken;
    private String clinicId;

    @BeforeEach
    void setUp() throws Exception {
        ownerToken = registerProfessionalAndGetToken(
                "Dr. Dono", "dono.clinic@example.com", "senha123", "22233344455", "CRM-SP-55001");

        otherToken = registerProfessionalAndGetToken(
                "Dr. Outro", "outro.clinic@example.com", "outro123", "55566677799", "CRM-SP-55002");

        clinicId = createClinicAndGetId(ownerToken, "Clínica Principal");
    }

    private String registerProfessionalAndGetToken(
            String name, String email, String password, String cpf, String crm) throws Exception {

        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password)
                .cpf(cpf).phone("11900000000")
                .birthDate(LocalDate.of(1980, 1, 1)).gender(Gender.MALE)
                .build();

        String regResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String userId = objectMapper.readTree(regResp).get("id").asText();
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(user);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user).profession("Médico").specialty("Clínica Geral")
                .licenseNumber(crm).status(ProfessionalProfileStatus.ACTIVE)
                .build();
        professionalProfileRepository.saveAndFlush(profile);

        String loginResp = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder().email(email).password(password).build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(loginResp).get("token").asText();
    }

    private String createClinicAndGetId(String token, String name) throws Exception {
        Map<String, Object> body = Map.of(
                "name", name,
                "description", "Clínica de teste",
                "phone", "1133334444",
                "address", "Rua Teste, 1",
                "city", "São Paulo",
                "state", "SP",
                "zipCode", "01310100"
        );
        String resp = mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(resp).get("id").asText();
    }

    // --------------- GET /clinics/{id}/working-hours ---------------

    @Test
    void getWorkingHours_semDados_retornaListaVazia() throws Exception {
        mockMvc.perform(get("/clinics/" + clinicId + "/working-hours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getWorkingHours_clinicaInexistente_retornaListaVazia() throws Exception {
        // Spring returns 200 with empty list since findByClinicId returns [] for unknown id
        mockMvc.perform(get("/clinics/id-inexistente/working-hours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getWorkingHours_semAutenticacao_retornaOk() throws Exception {
        // endpoint is public (no @PreAuthorize on GET)
        mockMvc.perform(get("/clinics/" + clinicId + "/working-hours"))
                .andExpect(status().isOk());
    }

    // --------------- PUT /clinics/{id}/working-hours ---------------

    @Test
    void saveWorkingHours_proprietario_salvaSucesso() throws Exception {
        List<Map<String, Object>> payload = List.of(
                Map.of("dayOfWeek", "MONDAY", "openTime", "08:00",
                        "closeTime", "18:00", "isOpen", true),
                Map.of("dayOfWeek", "TUESDAY", "openTime", "08:00",
                        "closeTime", "18:00", "isOpen", true)
        );

        mockMvc.perform(put("/clinics/" + clinicId + "/working-hours")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dayOfWeek", equalTo("MONDAY")))
                .andExpect(jsonPath("$[0].openTime", equalTo("08:00")))
                .andExpect(jsonPath("$[0].closeTime", equalTo("18:00")))
                .andExpect(jsonPath("$[0].isOpen", equalTo(true)))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].clinicId", equalTo(clinicId)));
    }

    @Test
    void saveWorkingHours_semanaCompleta_retorna7Registros() throws Exception {
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        List<Map<String, Object>> payload = java.util.Arrays.stream(days)
                .map(day -> Map.<String, Object>of(
                        "dayOfWeek", day,
                        "openTime", "08:00",
                        "closeTime", "18:00",
                        "isOpen", !day.equals("SATURDAY") && !day.equals("SUNDAY")
                ))
                .toList();

        mockMvc.perform(put("/clinics/" + clinicId + "/working-hours")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)));
    }

    @Test
    void saveWorkingHours_entaoGetPublico_retornaDados() throws Exception {
        List<Map<String, Object>> payload = List.of(
                Map.of("dayOfWeek", "WEDNESDAY", "openTime", "07:00",
                        "closeTime", "13:00", "isOpen", true)
        );

        mockMvc.perform(put("/clinics/" + clinicId + "/working-hours")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/clinics/" + clinicId + "/working-hours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dayOfWeek", equalTo("WEDNESDAY")))
                .andExpect(jsonPath("$[0].openTime", equalTo("07:00")));
    }

    @Test
    void saveWorkingHours_upsert_atualizaDiaExistente() throws Exception {
        List<Map<String, Object>> primeira = List.of(
                Map.of("dayOfWeek", "THURSDAY", "openTime", "09:00",
                        "closeTime", "17:00", "isOpen", true)
        );

        mockMvc.perform(put("/clinics/" + clinicId + "/working-hours")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(primeira)))
                .andExpect(status().isOk());

        List<Map<String, Object>> segunda = List.of(
                Map.of("dayOfWeek", "THURSDAY", "openTime", "10:00",
                        "closeTime", "20:00", "isOpen", false)
        );

        mockMvc.perform(put("/clinics/" + clinicId + "/working-hours")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(segunda)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/clinics/" + clinicId + "/working-hours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].openTime", equalTo("10:00")))
                .andExpect(jsonPath("$[0].closeTime", equalTo("20:00")))
                .andExpect(jsonPath("$[0].isOpen", equalTo(false)));
    }

    @Test
    void saveWorkingHours_naoProprietario_retorna400() throws Exception {
        List<Map<String, Object>> payload = List.of(
                Map.of("dayOfWeek", "FRIDAY", "openTime", "08:00",
                        "closeTime", "18:00", "isOpen", true)
        );

        mockMvc.perform(put("/clinics/" + clinicId + "/working-hours")
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveWorkingHours_semAutenticacao_retorna401() throws Exception {
        List<Map<String, Object>> payload = List.of(
                Map.of("dayOfWeek", "MONDAY", "openTime", "08:00",
                        "closeTime", "18:00", "isOpen", true)
        );

        mockMvc.perform(put("/clinics/" + clinicId + "/working-hours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void saveWorkingHours_clinicaInexistente_retorna404() throws Exception {
        List<Map<String, Object>> payload = List.of(
                Map.of("dayOfWeek", "MONDAY", "openTime", "08:00",
                        "closeTime", "18:00", "isOpen", true)
        );

        mockMvc.perform(put("/clinics/id-inexistente/working-hours")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
    }
}
