package com.consultafacil.api.controller;

import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GetQueueService — GET /appointments/queue.
 */
class GetQueueTest extends QueueAndCheckInTestBase {

    @Test
    void testGetQueueReturnsTodaysCheckedInAppointments() throws Exception {
        createAppointment(AppointmentStatus.CHECKED_IN);
        createAppointment(AppointmentStatus.IN_PROGRESS);
        createAppointment(AppointmentStatus.CONFIRMED); // should NOT appear

        mockMvc.perform(get("/appointments/queue")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status",
                        containsInAnyOrder("CHECKED_IN", "IN_PROGRESS")));
    }

    @Test
    void testGetQueueEmptyWhenNoneCheckedIn() throws Exception {
        createAppointment(AppointmentStatus.CONFIRMED);

        mockMvc.perform(get("/appointments/queue")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetQueueAsAdmin() throws Exception {
        createAppointment(AppointmentStatus.CHECKED_IN);

        User profUser = userRepository.findById(professionalUserId).orElseThrow();
        profUser.setRole(UserRole.ADMIN);
        userRepository.saveAndFlush(profUser);
        String adminToken = loginToken("queue.doctor@test.com", "password1");

        mockMvc.perform(get("/appointments/queue")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // Restore role
        profUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(profUser);
    }
}
