package com.consultafacil.api.controller;

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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de regressão para GET /professionals com paginação e filtros.
 * Garante que a rota nunca retorna 500, independente dos parâmetros enviados.
 */
@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class ProfessionalsListIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfessionalProfileRepository professionalProfileRepository;

    // IDs das profiles criadas no setUp para asserts pontuais
    private String medicoCardiologiaId;
    private String psicologoTccId;

    @BeforeEach
    void setUp() throws Exception {
        // Profissional 1: Médico / Cardiologia / com nome "Dr. João Silva"
        medicoCardiologiaId = criarProfissional(
                "Dr. João Silva", "joao.silva@example.com", "prof1234",
                "11111111101", "11900000101",
                "Médico", "Cardiologia", "CRM-SP-10001", Gender.MALE
        );

        // Profissional 2: Médico / Neurologia / com nome "Dra. Ana Costa"
        criarProfissional(
                "Dra. Ana Costa", "ana.costa@example.com", "prof1234",
                "22222222202", "11900000202",
                "Médico", "Neurologia", "CRM-SP-10002", Gender.FEMALE
        );

        // Profissional 3: Psicólogo / TCC / com nome "Carlos Mendes"
        psicologoTccId = criarProfissional(
                "Carlos Mendes", "carlos.mendes@example.com", "prof1234",
                "33333333303", "11900000303",
                "Psicólogo", "TCC", "CRP-SP-10003", Gender.MALE
        );

        // Profissional 4: Nutricionista / Nutrição Clínica
        criarProfissional(
                "Beatriz Oliveira", "beatriz@example.com", "prof1234",
                "44444444404", "11900000404",
                "Nutricionista", "Nutrição Clínica", "CRN-SP-10004", Gender.FEMALE
        );

        // Profissional 5: profession=null (dado legado sem migração)
        criarProfissionalSemProfissao(
                "Legado Sem Profissao", "legado@example.com", "prof1234",
                "55555555505", "11900000505",
                "Fisioterapia", "CREFITO-SP-10005", Gender.MALE
        );
    }

    // ─── Paginação explícita (exato URL que retornava 500) ─────────────────────

    @Test
    void listarComPaginacaoExplicita_deveRetornar200() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.number", equalTo(0)))
                .andExpect(jsonPath("$.size", equalTo(12)));
    }

    @Test
    void listarComPaginacaoExplicita_deveRetornarCampoProfession() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].profession").exists())
                .andExpect(jsonPath("$.content[0].specialty").exists())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].name").exists());
    }

    @Test
    void listarComPageSize1_deveCalcularTotalPaginasCorretamente() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(5)));
    }

    @Test
    void listarSegundaPagina_deveRetornar200() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number", equalTo(1)))
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    // ─── Filtro por profession ──────────────────────────────────────────────────

    @Test
    void filtrarPorProfession_deveRetornarApenasProfissionaisDaProfissao() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "Médico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].profession", everyItem(equalTo("Médico"))))
                .andExpect(jsonPath("$.totalElements", equalTo(2)));
    }

    @Test
    void filtrarPorProfession_naoExistente_deveRetornar200ComListaVazia() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "ProfissaoQueNaoExiste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void filtrarPorProfession_caseInsensitive_deveFuncionar() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "médico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)));
    }

    // ─── Filtro por specialty ───────────────────────────────────────────────────

    @Test
    void filtrarPorSpecialty_deveRetornarApenasProfissionaisDaEspecialidade() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("specialty", "Cardiologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].specialty", everyItem(equalTo("Cardiologia"))))
                .andExpect(jsonPath("$.totalElements", equalTo(1)));
    }

    @Test
    void filtrarPorSpecialty_naoExistente_deveRetornar200ComListaVazia() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("specialty", "EspecialidadeInexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void filtrarPorSpecialty_caseInsensitive_deveFuncionar() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("specialty", "cardiologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    // ─── Filtro por nome ────────────────────────────────────────────────────────

    @Test
    void filtrarPorNome_deveRetornarApenasProfissionaisComNomeCorrespondente() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("name", "João"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)))
                .andExpect(jsonPath("$.content[0].name", containsStringIgnoringCase("João")));
    }

    @Test
    void filtrarPorNome_naoExistente_deveRetornar200ComListaVazia() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("name", "NomeQueNaoExisteNoBanco99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void filtrarPorNome_caseInsensitive_deveFuncionar() throws Exception {
        // "silva" (lowercase) deve encontrar "Dr. João Silva" sem diferenciar maiúsculas
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("name", "silva"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    // ─── Filtros combinados ─────────────────────────────────────────────────────

    @Test
    void filtrarPorProfessionESpecialty_deveAplicarAmbosOsFiltros() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "Médico")
                        .param("specialty", "Cardiologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)))
                .andExpect(jsonPath("$.content[0].profession", equalTo("Médico")))
                .andExpect(jsonPath("$.content[0].specialty", equalTo("Cardiologia")));
    }

    @Test
    void filtrarPorProfessionENome_deveAplicarAmbosOsFiltros() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "Médico")
                        .param("name", "Ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)))
                .andExpect(jsonPath("$.content[0].name", containsStringIgnoringCase("Ana")));
    }

    @Test
    void filtrarPorTodosCampos_deveAplicarTodosFiltros() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "Médico")
                        .param("specialty", "Neurologia")
                        .param("name", "Ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)));
    }

    @Test
    void filtrosCombinados_semResultados_deveRetornar200ComListaVazia() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "Médico")
                        .param("specialty", "TCC"))  // TCC pertence a Psicólogo, não Médico
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    // ─── Dados legados (profession=null) não causam 500 ────────────────────────

    @Test
    void profissionalSemProfession_deveSerRetornadoSemErro() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.specialty == 'Fisioterapia')]", hasSize(1)));
    }

    @Test
    void filtrarPorProfession_comDadosLegados_naoDeveCausar500() throws Exception {
        // O profissional legado (profession=null) não deve aparecer ao filtrar por profissão
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "Médico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.specialty == 'Fisioterapia')]", hasSize(0)));
    }

    // ─── Estrutura da resposta paginada ────────────────────────────────────────

    @Test
    void respostaPaginada_deveConterCamposObrigatorios() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.number").isNumber())
                .andExpect(jsonPath("$.size").isNumber())
                .andExpect(jsonPath("$.first").isBoolean())
                .andExpect(jsonPath("$.last").isBoolean());
    }

    @Test
    void itemDaLista_deveConterCamposDeProfissional() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "Médico")
                        .param("specialty", "Cardiologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").isString())
                .andExpect(jsonPath("$.content[0].name").isString())
                .andExpect(jsonPath("$.content[0].email").isString())
                .andExpect(jsonPath("$.content[0].profession", equalTo("Médico")))
                .andExpect(jsonPath("$.content[0].specialty", equalTo("Cardiologia")))
                .andExpect(jsonPath("$.content[0].licenseNumber").isString())
                .andExpect(jsonPath("$.content[0].status").isString())
                .andExpect(jsonPath("$.content[0].consultationCount").isNumber());
    }

    @Test
    void profissionalEspecifico_deveConterIdCorreto() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("specialty", "Cardiologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", equalTo(medicoCardiologiaId)));
    }

    @Test
    void filtrarPorProfissaoPsicologo_deveRetornarApenasIds() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "Psicólogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)))
                .andExpect(jsonPath("$.content[0].id", equalTo(psicologoTccId)));
    }

    // ─── Sem filtros (listagem geral) ───────────────────────────────────────────

    @Test
    void semFiltros_deveRetornarTodosProfissionaisAtivos() throws Exception {
        mockMvc.perform(get("/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(5)));
    }

    @Test
    void semFiltros_filtrosVazios_deveTratarComoSemFiltro() throws Exception {
        // Parâmetros vazios devem ser ignorados (não filtrar)
        mockMvc.perform(get("/professionals")
                        .param("profession", "")
                        .param("specialty", "")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(5)));
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private String criarProfissional(
            String name, String email, String password,
            String cpf, String phone,
            String profession, String specialty, String licenseNumber,
            Gender gender) throws Exception {

        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password)
                .cpf(cpf).phone(phone)
                .birthDate(LocalDate.of(1985, 1, 1))
                .gender(gender)
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
                .user(user)
                .profession(profession)
                .specialty(specialty)
                .licenseNumber(licenseNumber)
                .status(ProfessionalProfileStatus.ACTIVE)
                .build();

        return professionalProfileRepository.saveAndFlush(profile).getId();
    }

    private void criarProfissionalSemProfissao(
            String name, String email, String password,
            String cpf, String phone,
            String specialty, String licenseNumber,
            Gender gender) throws Exception {

        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password)
                .cpf(cpf).phone(phone)
                .birthDate(LocalDate.of(1990, 6, 15))
                .gender(gender)
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

        // profession=null simula dado legado anterior à migration V11
        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user)
                .profession(null)
                .specialty(specialty)
                .licenseNumber(licenseNumber)
                .status(ProfessionalProfileStatus.ACTIVE)
                .build();

        professionalProfileRepository.saveAndFlush(profile);
    }
}
