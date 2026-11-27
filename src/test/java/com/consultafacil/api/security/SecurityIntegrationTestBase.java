package com.consultafacil.api.security;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.domain.repository.user.UserRepository;
import tools.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
abstract class SecurityIntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @MockitoBean StoragePort storagePort;

    @Value("${jwt.secret:test-secret-key-that-is-long-enough-for-hs512-algorithm-at-least-64-characters}")
    String jwtSecret;

    String patientToken;
    String patientUserId;
    String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        patientUserId = registerAndGetId("security-patient@example.com", "secpassword1", "11122233301");
        patientToken = loginAndGetToken("security-patient@example.com", "secpassword1");

        String adminUserId = registerAndGetId("security-admin@example.com", "adminpassword1", "99988877701");
        promoteToAdmin(adminUserId);
        adminToken = loginAndGetToken("security-admin@example.com", "adminpassword1");
    }

    String registerAndGetId(String email, String password, String cpf) throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name("Security Test User")
                .email(email)
                .password(password)
                .cpf(cpf)
                .phone("11999990001")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .build();

        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    String loginAndGetToken(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequestDTO.builder().email(email).password(password).build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    void promoteToAdmin(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(UserRole.ADMIN);
        userRepository.saveAndFlush(user);
    }

    String buildExpiredToken(String userId, String email) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date past = new Date(System.currentTimeMillis() - 10_000);
        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .claim("role", "PATIENT")
                .setIssuedAt(new Date(System.currentTimeMillis() - 20_000))
                .setExpiration(past)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
