package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.port.out.StoragePort;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.repository.UserRepository;
import com.example.demo.DemoApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private StoragePort storagePort;

    private String userToken;
    private String userId;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name("Test User")
                .email("usercontroller@example.com")
                .password("password123")
                .cpf("11122233344")
                .phone("11999990000")
                .birthDate(LocalDate.of(1992, 3, 15))
                .gender(Gender.FEMALE)
                .build();

        String regResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        userId = objectMapper.readTree(regResponse).get("id").asText();

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("usercontroller@example.com")
                        .password("password123")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        userToken = objectMapper.readTree(loginResponse).get("token").asText();

        // Register and promote admin user
        CreateUserDTO adminDTO = CreateUserDTO.builder()
                .name("Admin User")
                .email("admin@example.com")
                .password("adminpass123")
                .cpf("99988877766")
                .phone("11911112222")
                .birthDate(LocalDate.of(1985, 6, 10))
                .gender(Gender.MALE)
                .build();

        String adminRegResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String adminUserId = objectMapper.readTree(adminRegResponse).get("id").asText();
        User adminUser = userRepository.findById(adminUserId).orElseThrow();
        adminUser.setRole(UserRole.ADMIN);
        userRepository.saveAndFlush(adminUser);

        String adminLoginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("admin@example.com")
                        .password("adminpass123")
                        .build())))
                .andReturn().getResponse().getContentAsString();

        adminToken = objectMapper.readTree(adminLoginResponse).get("token").asText();
    }

    @Test
    void testGetCurrentUser() throws Exception {
        mockMvc.perform(get("/users/me")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(userId)))
                .andExpect(jsonPath("$.email", equalTo("usercontroller@example.com")))
                .andExpect(jsonPath("$.name", equalTo("Test User")))
                .andExpect(jsonPath("$.role", equalTo("PATIENT")));
    }

    @Test
    void testGetCurrentUserHasNullImageUrlByDefault() throws Exception {
        mockMvc.perform(get("/users/me")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl", nullValue()));
    }

    @Test
    void testGetCurrentUserUnauthorized() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUploadAvatar() throws Exception {
        String fakeUrl = "https://consulta-facil-images.s3.us-east-1.amazonaws.com/avatars/test-uuid.jpg";
        when(storagePort.upload(any(), any(), any(), eq("avatars"))).thenReturn(fakeUrl);

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-data".getBytes());

        mockMvc.perform(multipart("/users/me/avatar")
                .file(file)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(userId)))
                .andExpect(jsonPath("$.imageUrl", equalTo(fakeUrl)));
    }

    @Test
    void testUploadAvatarUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-data".getBytes());

        mockMvc.perform(multipart("/users/me/avatar").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetUserByIdRequiresAdmin() throws Exception {
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserByIdAsAdmin() throws Exception {
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(userId)))
                .andExpect(jsonPath("$.email", equalTo("usercontroller@example.com")));
    }

    @Test
    void testDeleteUserRequiresAdmin() throws Exception {
        mockMvc.perform(delete("/users/" + userId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteUserAsAdmin() throws Exception {
        mockMvc.perform(delete("/users/" + userId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUserUnauthorized() throws Exception {
        mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetUserByIdNotFoundAsAdmin() throws Exception {
        mockMvc.perform(get("/users/non-existent-uuid")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
