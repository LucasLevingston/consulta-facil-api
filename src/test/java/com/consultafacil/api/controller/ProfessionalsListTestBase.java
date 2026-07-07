package com.consultafacil.api.controller;

import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.professional.profile.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.user.UserRepository;
import com.consultafacil.ConsultaFacilApplication;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base compartilhada dos testes de regressão de GET /professionals.
 * Concentra setup e helpers comuns; sem métodos @Test (ver subclasses).
 */
@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
abstract class ProfessionalsListTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProfessionalProfileRepository professionalProfileRepository;

    protected String medicoCardiologiaId;
    protected String psicologoTccId;

    @BeforeEach
    void setUp() throws Exception {
        medicoCardiologiaId = criarProfissional("Dr. João Silva", "joao.silva@example.com", "prof1234",
                "11111111101", "11900000101", ProfessionalType.MEDICO, Specialty.CARDIOLOGIA, "CRM-SP-10001", Gender.MALE);
        criarProfissional("Dra. Ana Costa", "ana.costa@example.com", "prof1234",
                "22222222202", "11900000202", ProfessionalType.MEDICO, Specialty.NEUROLOGIA, "CRM-SP-10002", Gender.FEMALE);
        psicologoTccId = criarProfissional("Carlos Mendes", "carlos.mendes@example.com", "prof1234",
                "33333333303", "11900000303", ProfessionalType.PSICOLOGO, Specialty.TCC, "CRP-SP-10003", Gender.MALE);
        criarProfissional("Beatriz Oliveira", "beatriz@example.com", "prof1234",
                "44444444404", "11900000404", ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_CLINICA, "CRN-SP-10004", Gender.FEMALE);
        criarProfissionalSemProfissao("Legado Sem Profissao", "legado@example.com", "prof1234",
                "55555555505", "11900000505", Specialty.FISIOTERAPIA_ORTOPEDICA, "CREFITO-SP-10005", Gender.MALE);
    }

    protected String criarProfissional(String name, String email, String password, String cpf, String phone,
            ProfessionalType profession, Specialty specialty, String licenseNumber, Gender gender) throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder().name(name).email(email).password(password)
                .cpf(cpf).phone(phone).birthDate(LocalDate.of(1985, 1, 1)).gender(gender).build();
        String regResp = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String userId = objectMapper.readTree(regResp).get("id").asText();
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(user);
        ProfessionalProfile profile = ProfessionalProfile.builder().user(user).profession(profession)
                .specialty(specialty).licenseNumber(licenseNumber).status(ProfessionalProfileStatus.ACTIVE).build();
        return professionalProfileRepository.saveAndFlush(profile).getId();
    }

    protected void criarProfissionalSemProfissao(String name, String email, String password, String cpf, String phone,
            Specialty specialty, String licenseNumber, Gender gender) throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder().name(name).email(email).password(password)
                .cpf(cpf).phone(phone).birthDate(LocalDate.of(1990, 6, 15)).gender(gender).build();
        String regResp = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String userId = objectMapper.readTree(regResp).get("id").asText();
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(user);
        ProfessionalProfile profile = ProfessionalProfile.builder().user(user).profession(null)
                .specialty(specialty).licenseNumber(licenseNumber).status(ProfessionalProfileStatus.ACTIVE).build();
        professionalProfileRepository.saveAndFlush(profile);
    }
}
