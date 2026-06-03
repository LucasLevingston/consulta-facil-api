package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.MagicLinkRequestDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.entity.MagicLinkToken;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.repository.MagicLinkTokenRepository;
import com.example.consulta.domain.repository.UserRepository;
import com.example.consulta.domain.port.out.EmailPort;
import com.example.demo.DemoApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class MagicLinkIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private MagicLinkTokenRepository magicLinkTokenRepository;

    @MockBean private EmailPort emailPort;

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Magic User")
                        .email("magic@example.com")
                        .password("password123")
                        .cpf("55566677788")
                        .phone("11988880000")
                        .birthDate(LocalDate.of(1995, 5, 20))
                        .gender(Gender.MALE)
                        .build())))
                .andExpect(status().isCreated());

        testUser = userRepository.findByEmail("magic@example.com").orElseThrow();
    }

    @Test
    void requestMagicLink_knownEmail_returns204() throws Exception {
        mockMvc.perform(post("/auth/magic-link")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new MagicLinkRequestDTO("magic@example.com"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void requestMagicLink_unknownEmail_returns204_noLeakage() throws Exception {
        mockMvc.perform(post("/auth/magic-link")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new MagicLinkRequestDTO("nobody@example.com"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void requestMagicLink_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/auth/magic-link")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyMagicLink_validToken_returnsJwt() throws Exception {
        // request link first
        mockMvc.perform(post("/auth/magic-link")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new MagicLinkRequestDTO("magic@example.com"))))
                .andExpect(status().isNoContent());

        String rawToken = magicLinkTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(testUser.getId()))
                .findFirst()
                .map(MagicLinkToken::getToken)
                .orElseThrow();

        mockMvc.perform(get("/auth/magic-link/verify")
                .param("token", rawToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.type", equalTo("Bearer")))
                .andExpect(jsonPath("$.userId", equalTo(testUser.getId())));
    }

    @Test
    void verifyMagicLink_expiredToken_returns400() throws Exception {
        MagicLinkToken expired = magicLinkTokenRepository.save(MagicLinkToken.builder()
                .user(testUser)
                .token("expired-token-abc123")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build());

        mockMvc.perform(get("/auth/magic-link/verify")
                .param("token", expired.getToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyMagicLink_alreadyUsedToken_returns400() throws Exception {
        MagicLinkToken usedToken = magicLinkTokenRepository.save(MagicLinkToken.builder()
                .user(testUser)
                .token("used-token-xyz789")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(true)
                .build());

        mockMvc.perform(get("/auth/magic-link/verify")
                .param("token", usedToken.getToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyMagicLink_invalidToken_returns404() throws Exception {
        mockMvc.perform(get("/auth/magic-link/verify")
                .param("token", "totally-fake-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void verifyMagicLink_tokenBecomesUsedAfterVerification() throws Exception {
        mockMvc.perform(post("/auth/magic-link")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new MagicLinkRequestDTO("magic@example.com"))))
                .andExpect(status().isNoContent());

        String rawToken = magicLinkTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(testUser.getId()))
                .findFirst()
                .map(MagicLinkToken::getToken)
                .orElseThrow();

        // first use: OK
        mockMvc.perform(get("/auth/magic-link/verify").param("token", rawToken))
                .andExpect(status().isOk());

        // second use: rejected
        mockMvc.perform(get("/auth/magic-link/verify").param("token", rawToken))
                .andExpect(status().isBadRequest());
    }
}
