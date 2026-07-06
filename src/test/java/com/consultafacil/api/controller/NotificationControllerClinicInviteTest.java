package com.consultafacil.api.controller;

import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerClinicInviteTest extends NotificationControllerTestBase {

    private String promoteOwnerAndLogin() throws Exception {
        User ownerUser = userRepository.findById(ownerUserId).orElseThrow();
        ownerUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(ownerUser);
        return loginToken("notif.owner@test.com", "password1");
    }

    @Test
    void testSendClinicInviteSuccess() throws Exception {
        String freshOwnerToken = promoteOwnerAndLogin();

        mockMvc.perform(post("/clinics/" + clinic.getId() + "/invites/" + professionalProfileId)
                .header("Authorization", "Bearer " + freshOwnerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testSendClinicInviteDuplicateFails() throws Exception {
        String freshOwnerToken = promoteOwnerAndLogin();

        mockMvc.perform(post("/clinics/" + clinic.getId() + "/invites/" + professionalProfileId)
                .header("Authorization", "Bearer " + freshOwnerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/clinics/" + clinic.getId() + "/invites/" + professionalProfileId)
                .header("Authorization", "Bearer " + freshOwnerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSendClinicInviteNonOwnerFails() throws Exception {
        mockMvc.perform(post("/clinics/" + clinic.getId() + "/invites/" + professionalProfileId)
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isBadRequest());
    }
}
