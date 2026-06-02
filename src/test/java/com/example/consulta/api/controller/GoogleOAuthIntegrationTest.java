package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.GoogleLoginRequestDTO;
import com.example.consulta.domain.port.out.GoogleOAuthPort;
import com.example.consulta.domain.port.out.GoogleOAuthPort.GoogleUserInfo;
import com.example.demo.DemoApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class GoogleOAuthIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private GoogleOAuthPort googleOAuthPort;

    @Test
    void googleLogin_newUser_createsAccountAndReturnsJwt() throws Exception {
        when(googleOAuthPort.verifyIdToken(anyString()))
                .thenReturn(new GoogleUserInfo("google-sub-001", "googleuser@gmail.com",
                        "Google User", "https://photo.url/pic.jpg"));

        mockMvc.perform(post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GoogleLoginRequestDTO("fake-id-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.type", equalTo("Bearer")))
                .andExpect(jsonPath("$.email", equalTo("googleuser@gmail.com")));
    }

    @Test
    void googleLogin_existingEmailUser_linksGoogleIdAndReturnsJwt() throws Exception {
        // create user via normal registration first
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Existing","email":"existing@gmail.com","password":"password123",
                         "cpf":"00011122233","phone":"11900000000","birthDate":"1990-01-01","gender":"MALE"}
                        """))
                .andExpect(status().isCreated());

        when(googleOAuthPort.verifyIdToken(anyString()))
                .thenReturn(new GoogleUserInfo("google-sub-002", "existing@gmail.com",
                        "Existing", null));

        mockMvc.perform(post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GoogleLoginRequestDTO("fake-id-token-2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", equalTo("existing@gmail.com")));
    }

    @Test
    void googleLogin_sameGoogleUser_loginsTwice() throws Exception {
        when(googleOAuthPort.verifyIdToken(anyString()))
                .thenReturn(new GoogleUserInfo("google-sub-003", "repeat@gmail.com",
                        "Repeat User", null));

        mockMvc.perform(post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GoogleLoginRequestDTO("token-a"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GoogleLoginRequestDTO("token-b"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", equalTo("repeat@gmail.com")));
    }

    @Test
    void googleLogin_emptyIdToken_returns400() throws Exception {
        mockMvc.perform(post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idToken\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
