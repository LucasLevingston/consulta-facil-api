package com.consultafacil.api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JwtAndRbacSecurityIntegrationTest extends SecurityIntegrationTestBase {

    @Test
    void jwtExpiredTokenReturns401() throws Exception {
        String expiredToken = buildExpiredToken(patientUserId, "security-patient@example.com");
        mockMvc.perform(get("/users/me").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtMalformedTokenReturns401() throws Exception {
        mockMvc.perform(get("/users/me").header("Authorization", "Bearer this.is.not.a.valid.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtRandomStringTokenReturns401() throws Exception {
        mockMvc.perform(get("/users/me").header("Authorization", "Bearer randomgarbagetoken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtMissingTokenReturns401() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtWrongSchemeReturns401() throws Exception {
        mockMvc.perform(get("/users/me").header("Authorization", "Basic " + patientToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtTokenSignedWithWrongKeyReturns401() throws Exception {
        String wrongKey = "wrong-secret-key-that-is-long-enough-for-hs512-algorithm-at-least-64-chars";
        SecretKey key = Keys.hmacShaKeyFor(wrongKey.getBytes(StandardCharsets.UTF_8));
        String tampered = Jwts.builder()
                .setSubject(patientUserId)
                .claim("email", "security-patient@example.com")
                .claim("role", "ADMIN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        mockMvc.perform(get("/users/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rbacPatientAccessingAdminGetUserByIdReturns403() throws Exception {
        mockMvc.perform(get("/users/" + patientUserId).header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void rbacPatientAccessingAdminDeleteUserReturns403() throws Exception {
        mockMvc.perform(delete("/users/" + patientUserId).header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void rbacAdminCanAccessGetUserById() throws Exception {
        mockMvc.perform(get("/users/" + patientUserId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(patientUserId)));
    }
}
