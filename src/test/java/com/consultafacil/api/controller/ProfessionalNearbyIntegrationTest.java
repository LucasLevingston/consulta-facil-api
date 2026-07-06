package com.consultafacil.api.controller;

import com.consultafacil.domain.entity.ProfessionalProfile;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfessionalNearbyIntegrationTest extends ClinicControllerTestBase {

    @Test
    void testGetDoctorsNearby_returnsDoctorWithinRadius() throws Exception {
        ProfessionalProfile dp = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        dp.setLatitude(-23.5505);
        dp.setLongitude(-46.6333);
        professionalProfileRepository.saveAndFlush(dp);

        mockMvc.perform(get("/professionals/nearby")
                .param("lat", "-23.5505")
                .param("lng", "-46.6333")
                .param("radiusKm", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id", equalTo(professionalProfileId)));
    }

    @Test
    void testGetDoctorsNearby_excludesFarDoctors() throws Exception {
        ProfessionalProfile dp = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        dp.setLatitude(-23.5505);
        dp.setLongitude(-46.6333);
        professionalProfileRepository.saveAndFlush(dp);

        // Query from Manaus
        mockMvc.perform(get("/professionals/nearby")
                .param("lat", "-3.1316")
                .param("lng", "-60.0213")
                .param("radiusKm", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetDoctorsNearby_filtersBySpecialty() throws Exception {
        ProfessionalProfile dp1 = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        dp1.setLatitude(-23.5505);
        dp1.setLongitude(-46.6333);
        professionalProfileRepository.saveAndFlush(dp1);

        ProfessionalProfile dp2 = professionalProfileRepository.findById(secondProfessionalProfileId).orElseThrow();
        dp2.setLatitude(-23.5510);
        dp2.setLongitude(-46.6340);
        professionalProfileRepository.saveAndFlush(dp2);

        // Both are nearby, but filter by Cardiologia only gets dp1
        mockMvc.perform(get("/professionals/nearby")
                .param("lat", "-23.5505")
                .param("lng", "-46.6333")
                .param("radiusKm", "10")
                .param("specialty", "Cardiologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].specialty", equalTo("CARDIOLOGIA")));
    }

    @Test
    void testGetDoctorsNearby_excludesDoctorsWithoutCoordinates() throws Exception {
        // professionalProfileId has no coordinates set — should not appear
        mockMvc.perform(get("/professionals/nearby")
                .param("lat", "-23.5505")
                .param("lng", "-46.6333")
                .param("radiusKm", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetDoctorsNearby_isPublic() throws Exception {
        mockMvc.perform(get("/professionals/nearby")
                .param("lat", "-23.5505")
                .param("lng", "-46.6333"))
                .andExpect(status().isOk());
    }
}
