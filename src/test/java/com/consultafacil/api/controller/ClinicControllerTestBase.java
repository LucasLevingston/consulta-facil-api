package com.consultafacil.api.controller;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
abstract class ClinicControllerTestBase extends ClinicControllerFixtures {

    protected String doctorToken;
    protected String doctorUserId;
    protected String professionalProfileId;
    protected String adminToken;
    protected String secondDoctorToken;
    protected String secondProfessionalProfileId;

    @BeforeEach
    void setUp() throws Exception {
        doctorUserId = registerUser("Dr. João", "joao@example.com", "senha123",
                "11122233344", "11900000001", LocalDate.of(1980, 1, 1), Gender.MALE);
        promoteRole(doctorUserId, UserRole.PROFESSIONAL);
        professionalProfileId = createDoctorProfile(doctorUserId, Specialty.CARDIOLOGIA, "CRM-SP-11111");
        doctorToken = loginAndGetToken("joao@example.com", "senha123");

        String adminId = registerUser("Admin", "admin@clinic.com", "admin123",
                "99988877766", "11900000099", LocalDate.of(1975, 6, 15), Gender.FEMALE);
        promoteRole(adminId, UserRole.ADMIN);
        adminToken = loginAndGetToken("admin@clinic.com", "admin123");

        String secondId = registerUser("Dr. Maria", "maria@example.com", "senha456",
                "55566677788", "11900000002", LocalDate.of(1985, 5, 10), Gender.FEMALE);
        promoteRole(secondId, UserRole.PROFESSIONAL);
        secondProfessionalProfileId = createDoctorProfile(secondId, Specialty.NEUROLOGIA, "CRM-SP-22222");
        secondDoctorToken = loginAndGetToken("maria@example.com", "senha456");
    }
}
