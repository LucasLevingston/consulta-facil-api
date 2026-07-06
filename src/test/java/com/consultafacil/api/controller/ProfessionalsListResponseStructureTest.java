package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GET /professionals — estrutura da resposta paginada e cenário sem filtros.
 */
class ProfessionalsListResponseStructureTest extends ProfessionalsListTestBase {

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
                        .param("profession", "MEDICO")
                        .param("specialty", "CARDIOLOGIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").isString())
                .andExpect(jsonPath("$.content[0].name").isString())
                .andExpect(jsonPath("$.content[0].email").isString())
                .andExpect(jsonPath("$.content[0].profession", equalTo("MEDICO")))
                .andExpect(jsonPath("$.content[0].specialty", equalTo("CARDIOLOGIA")))
                .andExpect(jsonPath("$.content[0].licenseNumber").isString())
                .andExpect(jsonPath("$.content[0].status").isString())
                .andExpect(jsonPath("$.content[0].consultationCount").isNumber());
    }

    @Test
    void profissionalEspecifico_deveConterIdCorreto() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("specialty", "CARDIOLOGIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", equalTo(medicoCardiologiaId)));
    }

    @Test
    void filtrarPorProfissaoPsicologo_deveRetornarApenasIds() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("page", "0")
                        .param("size", "12")
                        .param("profession", "PSICOLOGO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)))
                .andExpect(jsonPath("$.content[0].id", equalTo(psicologoTccId)));
    }

    @Test
    void semFiltros_deveRetornarTodosProfissionaisAtivos() throws Exception {
        mockMvc.perform(get("/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(5)));
    }

    @Test
    void semFiltros_filtrosVazios_deveTratarComoSemFiltro() throws Exception {
        mockMvc.perform(get("/professionals")
                        .param("profession", "")
                        .param("specialty", "")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(5)));
    }
}
