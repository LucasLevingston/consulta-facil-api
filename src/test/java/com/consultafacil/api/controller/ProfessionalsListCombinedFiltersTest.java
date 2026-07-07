package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GET /professionals — combinações de filtros (profession + specialty + name).
 */
class ProfessionalsListCombinedFiltersTest extends ProfessionalsListTestBase {

    @Test
    void filtrarPorProfessionESpecialty_deveAplicarAmbosOsFiltros() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "MEDICO")
                        .param("specialty", "CARDIOLOGIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)))
                .andExpect(jsonPath("$.content[0].profession", equalTo("MEDICO")))
                .andExpect(jsonPath("$.content[0].specialty", equalTo("CARDIOLOGIA")));
    }

    @Test
    void filtrarPorProfessionENome_deveAplicarAmbosOsFiltros() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "MEDICO")
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
                        .param("profession", "MEDICO")
                        .param("specialty", "NEUROLOGIA")
                        .param("name", "Ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)));
    }

    @Test
    void filtrosCombinados_semResultados_deveRetornar200ComListaVazia() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "MEDICO")
                        .param("specialty", "TCC"))  // TCC pertence a Psicólogo, não Médico
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }
}
