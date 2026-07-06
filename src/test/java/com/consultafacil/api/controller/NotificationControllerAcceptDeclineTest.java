package com.consultafacil.api.controller;

import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.enums.NotificationStatus;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerAcceptDeclineTest extends NotificationControllerTestBase {

    @Test
    void testAcceptInviteSuccess() throws Exception {
        Notification n = createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/" + n.getId() + "/accept")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("ACCEPTED")));
    }

    @Test
    void testAcceptAlreadyRespondedInviteFails() throws Exception {
        Notification n = createInvite(NotificationStatus.ACCEPTED);

        mockMvc.perform(put("/notifications/" + n.getId() + "/accept")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAcceptInviteWrongUserFails() throws Exception {
        Notification n = createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/" + n.getId() + "/accept")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeclineInviteSuccess() throws Exception {
        Notification n = createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/" + n.getId() + "/decline")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("DECLINED")));
    }

    @Test
    void testDeclineAlreadyRespondedInviteFails() throws Exception {
        Notification n = createInvite(NotificationStatus.DECLINED);

        mockMvc.perform(put("/notifications/" + n.getId() + "/decline")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeclineInviteWrongUserFails() throws Exception {
        Notification n = createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/" + n.getId() + "/decline")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest());
    }
}
