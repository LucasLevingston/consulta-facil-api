package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfessionalControllerListIntegrationTest extends ProfessionalControllerTestBase {

    @Test
    void testListProfessionals() throws Exception {
        mockMvc.perform(get("/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    void testSearchProfessionalsBySpecialty() throws Exception {
        mockMvc.perform(get("/professionals/search").param("specialty", "Cardiologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].specialty", equalTo("CARDIOLOGIA")));
    }

    @Test
    void testSearchBySpecialtyNoResults() throws Exception {
        mockMvc.perform(get("/professionals/search").param("specialty", "EspecialidadeInexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void testGetProfessionalById() throws Exception {
        mockMvc.perform(get("/professionals/" + professionalProfileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(professionalProfileId)))
                .andExpect(jsonPath("$.specialty", equalTo("CARDIOLOGIA")));
    }

    @Test
    void testGetProfessionalByIdNotFound() throws Exception {
        mockMvc.perform(get("/professionals/non-existent-id"))
                .andExpect(status().isNotFound());
    }
}
