package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GET /professionals — filtro por profession, incluindo dados legados (profession=null).
 */
class ProfessionalsListProfessionFilterTest extends ProfessionalsListTestBase {

    @Test
    void filtrarPorProfession_deveRetornarApenasProfissionaisDaProfissao() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "MEDICO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].profession", everyItem(equalTo("MEDICO"))))
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
                        .param("profession", "medico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)));
    }

    @Test
    void profissionalSemProfession_deveSerRetornadoSemErro() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.specialty == 'FISIOTERAPIA_ORTOPEDICA')]", hasSize(1)));
    }

    @Test
    void filtrarPorProfession_comDadosLegados_naoDeveCausar500() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "MEDICO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.specialty == 'FISIOTERAPIA_ORTOPEDICA')]", hasSize(0)));
    }
}
