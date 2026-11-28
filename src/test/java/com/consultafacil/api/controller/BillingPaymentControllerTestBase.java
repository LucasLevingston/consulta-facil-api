package com.consultafacil.api.controller;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.PaymentType;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.domain.port.out.billing.PaymentGatewayPort;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
abstract class BillingPaymentControllerTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;

    @MockitoBean
    protected StoragePort storagePort;

    @MockitoBean
    protected PaymentGatewayPort paymentGateway;

    protected String patientToken;
    protected String patientUserId;
    protected String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // The PaymentGatewayPort is a dependency of CreateBillingPaymentService; mock it
        when(paymentGateway.gatewayName()).thenReturn("MOCK");
        when(paymentGateway.createPayment(any())).thenAnswer(invocation -> {
            BillingPayment p = invocation.getArgument(0);
            p.setGatewayPaymentId("gw-" + System.nanoTime());
            p.setStatus(BillingPaymentStatus.PENDING);
            if (p.getNetAmount() == null) {
                p.setNetAmount(p.getAmount());
            }
            return p;
        });
        when(paymentGateway.cancelPayment(any())).thenAnswer(invocation -> {
            BillingPayment p = BillingPayment.builder()
                    .gatewayPaymentId(invocation.getArgument(0))
                    .status(BillingPaymentStatus.CANCELED)
                    .amount(BigDecimal.TEN)
                    .systemFee(BigDecimal.ZERO)
                    .gatewayFee(BigDecimal.ZERO)
                    .netAmount(BigDecimal.TEN)
                    .paymentType(PaymentType.CONSULTATION)
                    .currency("BRL")
                    .gateway("MOCK")
                    .build();
            return p;
        });
        when(paymentGateway.refundPayment(any(), any())).thenAnswer(invocation -> {
            BillingPayment p = BillingPayment.builder()
                    .gatewayPaymentId(invocation.getArgument(0))
                    .status(BillingPaymentStatus.REFUNDED)
                    .amount(BigDecimal.TEN)
                    .systemFee(BigDecimal.ZERO)
                    .gatewayFee(BigDecimal.ZERO)
                    .netAmount(BigDecimal.TEN)
                    .paymentType(PaymentType.CONSULTATION)
                    .currency("BRL")
                    .gateway("MOCK")
                    .build();
            return p;
        });

        patientUserId = registerUser(
                "Billing Patient", "billing-patient@example.com", "billingpass1",
                "41122233344", "11999991111");
        patientToken = loginAndGetToken("billing-patient@example.com", "billingpass1");

        String adminUserId = registerUser(
                "Billing Admin", "billing-admin@example.com", "billingadmin1",
                "88877766644", "11999992222");
        promoteTo(adminUserId, UserRole.ADMIN);
        adminToken = loginAndGetToken("billing-admin@example.com", "billingadmin1");
    }

    protected String registerUser(String name, String email, String password,
                                  String cpf, String phone) throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password).cpf(cpf)
                .phone(phone).birthDate(LocalDate.of(1988, 7, 25)).gender(Gender.MALE)
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
}
