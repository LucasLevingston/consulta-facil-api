package com.consultafacil.api.controller;

import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.enums.NotificationStatus;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerMarkReadTest extends NotificationControllerTestBase {

    @Test
    void testMarkAsReadSuccess() throws Exception {
        Notification n = createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/" + n.getId() + "/read")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("READ")));
    }

    @Test
    void testMarkAsReadWrongUserFails() throws Exception {
        Notification n = createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/" + n.getId() + "/read")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMarkAsReadAlreadyReadNoOp() throws Exception {
        Notification n = createInvite(NotificationStatus.READ);

        mockMvc.perform(put("/notifications/" + n.getId() + "/read")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("READ")));
    }

    @Test
    void testMarkAllAsReadSuccess() throws Exception {
        createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/read-all")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testMarkAllAsReadWithNoPendingSucceeds() throws Exception {
        mockMvc.perform(put("/notifications/read-all")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isNoContent());
    }
}
