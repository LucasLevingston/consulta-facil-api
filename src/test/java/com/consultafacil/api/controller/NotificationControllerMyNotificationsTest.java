package com.consultafacil.api.controller;

import com.consultafacil.domain.enums.NotificationStatus;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerMyNotificationsTest extends NotificationControllerTestBase {

    @Test
    void testGetMyNotificationsReturnsList() throws Exception {
        createInvite(NotificationStatus.PENDING);

        mockMvc.perform(get("/notifications/me")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].type", equalTo("CLINIC_INVITE")))
                .andExpect(jsonPath("$[0].status", equalTo("PENDING")));
    }

    @Test
    void testGetMyNotificationsEmptyWhenNone() throws Exception {
        mockMvc.perform(get("/notifications/me")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetMyNotificationsRequiresAuth() throws Exception {
        mockMvc.perform(get("/notifications/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUnreadCountReturnsPendingCount() throws Exception {
        createInvite(NotificationStatus.PENDING);

        mockMvc.perform(get("/notifications/me/unread-count")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", equalTo(1)));
    }

    @Test
    void testUnreadCountZeroWhenNoPending() throws Exception {
        createInvite(NotificationStatus.READ);

        mockMvc.perform(get("/notifications/me/unread-count")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", equalTo(0)));
    }
}
