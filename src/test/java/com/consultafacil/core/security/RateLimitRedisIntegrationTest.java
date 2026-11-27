package com.consultafacil.core.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "spring.cache.type=none",
        "auth.rate-limit.login.requests-per-minute=5",
        "auth.rate-limit.forgot-password.requests-per-15min=3"
})
class RateLimitRedisIntegrationTest {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    private static final String LOGIN_BODY = "{\"email\":\"test@test.com\",\"password\":\"wrong\"}";
    private static final String FORGOT_BODY = "{\"email\":\"test@test.com\"}";

    @Test
    void login_sixthRequest_returns429() throws Exception {
        String uniqueIp = "10.111.1.1";
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Forwarded-For", uniqueIp)
                            .content(LOGIN_BODY))
                    .andReturn(); // allowed through (401/400 expected, not 429)
        }

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", uniqueIp)
                        .content(LOGIN_BODY))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"));
    }

    @Test
    void forgotPassword_fourthRequest_returns429() throws Exception {
        String uniqueIp = "10.111.2.1";
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Forwarded-For", uniqueIp)
                            .content(FORGOT_BODY))
                    .andReturn(); // allowed through (401/400 expected, not 429)
        }

        mockMvc.perform(post("/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", uniqueIp)
                        .content(FORGOT_BODY))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "900"));
    }

    @Test
    void login_differentIps_independentBuckets() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Forwarded-For", "10.111.3." + i)
                            .content(LOGIN_BODY))
                    .andReturn(); // allowed through (401/400 expected, not 429)
        }
    }
}
