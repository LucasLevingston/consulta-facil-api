package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GET /professionals — filtros por specialty e por nome.
 */
class ProfessionalsListSpecialtyAndNameFilterTest extends ProfessionalsListTestBase {

    @Test
    void filtrarPorSpecialty_deveRetornarApenasProfissionaisDaEspecialidade() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("specialty", "Cardiologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].specialty", everyItem(equalTo("CARDIOLOGIA"))))
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
}
