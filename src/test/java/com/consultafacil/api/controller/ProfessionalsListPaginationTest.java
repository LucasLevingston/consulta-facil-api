package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GET /professionals — paginação explícita (exato URL que retornava 500).
 */
class ProfessionalsListPaginationTest extends ProfessionalsListTestBase {

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
}
