package com.consultafacil.api.controller;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.domain.entity.HealthInsurancePlan;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.domain.repository.insurance.HealthInsurancePlanRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class InsurancePlanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HealthInsurancePlanRepository planRepository;

    @MockitoBean
    private StoragePort storagePort;

    @BeforeEach
    void setUp() {
        planRepository.saveAndFlush(
                HealthInsurancePlan.builder().name("Unimed").code("UNIMED001").active(true).build());
        planRepository.saveAndFlush(
                HealthInsurancePlan.builder().name("Bradesco Saude").code("BRADESCO001").active(true).build());
        planRepository.saveAndFlush(
                HealthInsurancePlan.builder().name("Inactive Plan").code("INACTIVE001").active(false).build());
    }

    @Test
    void listPlans_returnsOnlyActivePlans() throws Exception {
        mockMvc.perform(get("/insurance/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$[*].active", everyItem(equalTo(true))));
    }

    @Test
    void listPlans_doesNotRequireAuth() throws Exception {
        // The endpoint has no @PreAuthorize — it must be accessible without a token
        mockMvc.perform(get("/insurance/plans"))
                .andExpect(status().isOk());
    }

    @Test
    void listPlans_containsSeededPlans() throws Exception {
        mockMvc.perform(get("/insurance/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("Unimed", "Bradesco Saude")));
    }

    @Test
    void listPlans_doesNotReturnInactivePlans() throws Exception {
        mockMvc.perform(get("/insurance/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", not(hasItem("Inactive Plan"))));
    }
}
