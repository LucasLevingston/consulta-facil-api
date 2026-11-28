package com.consultafacil.api.controller;

import com.consultafacil.api.dto.insurance.AddProfessionalInsuranceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfessionalInsuranceControllerIntegrationTest extends ProfessionalInsuranceControllerTestBase {

    @Test
    void addInsurance_professionalCanAddOwnPlan() throws Exception {
        AddProfessionalInsuranceRequest req = new AddProfessionalInsuranceRequest(planId, "Preferred plan");

        mockMvc.perform(post("/professionals/me/insurance")
                        .header("Authorization", "Bearer " + professionalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void addInsurance_requiresAuth() throws Exception {
        AddProfessionalInsuranceRequest req = new AddProfessionalInsuranceRequest(planId, null);

        mockMvc.perform(post("/professionals/me/insurance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addInsurance_forbiddenForPatient() throws Exception {
        AddProfessionalInsuranceRequest req = new AddProfessionalInsuranceRequest(planId, null);

        mockMvc.perform(post("/professionals/me/insurance")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addInsurance_returns400WhenPlanIdBlank() throws Exception {
        // planId is @NotBlank — empty string must fail validation
        AddProfessionalInsuranceRequest req = new AddProfessionalInsuranceRequest("", null);

        mockMvc.perform(post("/professionals/me/insurance")
                        .header("Authorization", "Bearer " + professionalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listInsurance_professionalSeesOwnPlans() throws Exception {
        // Add a plan first so the list is non-empty
        AddProfessionalInsuranceRequest req = new AddProfessionalInsuranceRequest(planId, "My plan");
        mockMvc.perform(post("/professionals/me/insurance")
                        .header("Authorization", "Bearer " + professionalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/professionals/me/insurance")
                        .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void listInsurance_requiresAuth() throws Exception {
        // /professionals/** is permitAll in security config; method security returns 403
        mockMvc.perform(get("/professionals/me/insurance"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listInsurance_forbiddenForPatient() throws Exception {
        mockMvc.perform(get("/professionals/me/insurance")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeInsurance_professionalCanRemoveOwnPlan() throws Exception {
        // Add first
        AddProfessionalInsuranceRequest req = new AddProfessionalInsuranceRequest(planId, null);
        mockMvc.perform(post("/professionals/me/insurance")
                        .header("Authorization", "Bearer " + professionalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Then remove
        mockMvc.perform(delete("/professionals/me/insurance/" + planId)
                        .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeInsurance_requiresAuth() throws Exception {
        // /professionals/** is permitAll in security config; method security returns 403
        mockMvc.perform(delete("/professionals/me/insurance/" + planId))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeInsurance_forbiddenForPatient() throws Exception {
        mockMvc.perform(delete("/professionals/me/insurance/" + planId)
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }
}
