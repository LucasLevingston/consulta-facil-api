package com.consultafacil.api.security;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.consultafacil.api.dto.auth.LoginRequestDTO;
import org.springframework.http.MediaType;

class DataProtectionSecurityIntegrationTest extends SecurityIntegrationTestBase {

    @Test
    void sqlInjectionInPathParamReturnsNonCrashing() throws Exception {
        int status = mockMvc.perform(get("/users/1' OR '1'='1")
                        .header("Authorization", "Bearer " + adminToken))
                .andReturn().getResponse().getStatus();
        assert status == 404 || status == 400 : "Expected 404 or 400, got " + status;
    }

    @Test
    void sqlInjectionDropTableInPathParamReturnsNonCrashing() throws Exception {
        int status = mockMvc.perform(get("/users/; DROP TABLE users")
                        .header("Authorization", "Bearer " + adminToken))
                .andReturn().getResponse().getStatus();
        assert status == 404 || status == 400 : "Expected 404 or 400, got " + status;
    }

    @Test
    void sqlInjectionInQueryParamReturnsNonCrashing() throws Exception {
        int status = mockMvc.perform(get("/professionals").param("name", "' OR 1=1 --"))
                .andReturn().getResponse().getStatus();
        assert status != 500 : "SQL injection in query param caused a 500 server error";
    }

    @Test
    void massAssignmentRoleFieldIgnoredOnRegister() throws Exception {
        String rawBody = """
                {
                    "name": "Injected Admin",
                    "email": "injected-admin@example.com",
                    "password": "password123",
                    "cpf": "55566677788",
                    "phone": "11988887777",
                    "birthDate": "1990-01-01",
                    "gender": "MALE",
                    "role": "ADMIN"
                }
                """;

        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String assignedRole = objectMapper.readTree(response).get("role").asText();
        assert "PATIENT".equals(assignedRole) : "Mass assignment: role should be PATIENT but was " + assignedRole;
    }

    @Test
    void corsPreflightFromAllowedOriginReturnsOk() throws Exception {
        mockMvc.perform(options("/users/me")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    void corsPreflightFromUnknownOriginDoesNotExposeAllowOriginHeader() throws Exception {
        mockMvc.perform(options("/users/me")
                        .header("Origin", "http://evil.example.com")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void loginResponseDoesNotContainRawPassword() throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequestDTO.builder()
                                        .email("security-patient@example.com")
                                        .password("secpassword1")
                                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assert !response.contains("secpassword1") : "Login response must not echo back the raw password";
        assert !response.contains("password") : "Login response must not contain any 'password' field";
    }

    @Test
    void registerResponseDoesNotContainRawPassword() throws Exception {
        String rawBody = """
                {
                    "name": "No Password Leak",
                    "email": "noleak@example.com",
                    "password": "supersecret99",
                    "cpf": "44455566677",
                    "phone": "11977776666",
                    "birthDate": "1988-05-15",
                    "gender": "FEMALE"
                }
                """;

        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assert !response.contains("supersecret99") : "Register response must not echo back the raw password";
        assert !response.contains("password") : "Register response must not contain any 'password' field";
    }

    @Test
    void errorResponseDoesNotExposeStackTrace() throws Exception {
        String response = mockMvc.perform(get("/users/non-existent-id-xyz")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        assert !response.contains("at com.") : "Error response must not contain Java stack trace";
        assert !response.contains("java.lang.") : "Error response must not contain Java class names";
        assert !response.contains("sun.reflect.") : "Error response must not contain reflection classes";
    }

    @Test
    void userMeResponseDoesNotContainPasswordField() throws Exception {
        String response = mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assert !response.contains("password") : "GET /users/me must not return a 'password' field";
    }
}
