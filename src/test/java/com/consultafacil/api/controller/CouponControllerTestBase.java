package com.consultafacil.api.controller;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.coupon.CreateCouponDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.domain.repository.user.UserRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
abstract class CouponControllerTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;

    @MockitoBean
    protected StoragePort storagePort;

    protected String adminToken;
    protected String adminUserId;
    protected String patientToken;
    protected String patientUserId;

    @BeforeEach
    void setUp() throws Exception {
        patientUserId = registerUser(
                "Coupon Patient", "coupon-patient@example.com", "couponpass1",
                "31122233344", "11999991111");
        patientToken = loginAndGetToken("coupon-patient@example.com", "couponpass1");

        adminUserId = registerUser(
                "Coupon Admin", "coupon-admin@example.com", "couponadmin1",
                "88877766655", "11999992222");
        promoteTo(adminUserId, UserRole.ADMIN);
        adminToken = loginAndGetToken("coupon-admin@example.com", "couponadmin1");
    }

    protected String registerUser(String name, String email, String password,
                                  String cpf, String phone) throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password).cpf(cpf)
                .phone(phone).birthDate(LocalDate.of(1990, 5, 20)).gender(Gender.MALE)
                .build();
        String resp = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }

    protected void promoteTo(String userId, UserRole role) {
        com.consultafacil.domain.entity.User user = userRepository.findById(userId).orElseThrow();
        user.setRole(role);
        userRepository.saveAndFlush(user);
    }

    protected String loginAndGetToken(String email, String password) throws Exception {
        String resp = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequestDTO.builder().email(email).password(password).build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }

    protected String createCouponAndGetId(String code) throws Exception {
        CreateCouponDTO dto = new CreateCouponDTO();
        dto.setCode(code);
        dto.setDescription("Test coupon");
        dto.setType(CouponType.PERCENT);
        dto.setValue(new BigDecimal("10.00"));
        dto.setMaxUsesPerUser(1);

        String resp = mockMvc.perform(post("/admin/billing/coupons/codes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }
}
