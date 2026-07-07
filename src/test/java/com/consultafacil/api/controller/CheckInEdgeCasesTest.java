package com.consultafacil.api.controller;

import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.Gender;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Casos de borda de check-in: token inexistente, paciente errado e tipo de token incorreto.
 */
class CheckInEdgeCasesTest extends QueueAndCheckInTestBase {

    @Test
    void testGetCheckInTokenNonExistentAppointmentFails() throws Exception {
        mockMvc.perform(get("/appointments/nonexistent-id-12345/checkin-token")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCheckInTokenWrongPatientServiceFails() throws Exception {
        // Create appointment for default patient
        Appointment appt = createAppointment(AppointmentStatus.CONFIRMED);

        // Register a second patient
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Queue Patient Two")
                        .email("queue.patient2@test.com")
                        .password("password1")
                        .cpf("99988877766")
                        .phone("11933333333")
                        .birthDate(LocalDate.of(1992, 3, 10))
                        .gender(Gender.MALE)
                        .build())))
                .andExpect(status().isCreated());
        String patient2Token = loginToken("queue.patient2@test.com", "password1");

        // Second patient tries to get token for first patient's appointment
        mockMvc.perform(get("/appointments/" + appt.getId() + "/checkin-token")
                .header("Authorization", "Bearer " + patient2Token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCheckInByQrWithWrongTokenTypeFails() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String wrongTypeToken = Jwts.builder()
                .subject("some-appointment-id")
                .claims(Map.of("type", "NOT_CHECKIN"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();

        mockMvc.perform(post("/appointments/checkin")
                .header("Authorization", "Bearer " + professionalToken)
                .param("token", wrongTypeToken))
                .andExpect(status().isBadRequest());
    }
}
