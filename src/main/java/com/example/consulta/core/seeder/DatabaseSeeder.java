package com.example.consulta.core.seeder;

import com.example.consulta.application.service.AppointmentService;
import com.example.consulta.application.service.DoctorService;
import com.example.consulta.application.service.UserService;
import com.example.consulta.api.dto.appointment.CreateAppointmentDTO;
import com.example.consulta.api.dto.doctor.CreateDoctorDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.repository.PatientProfileRepository;
import com.example.consulta.domain.repository.UserRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@Profile("!test")
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final UserService userService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    private final Faker faker = new Faker(new Locale("pt-BR"));
    private final List<String> specialties = List.of(
            "Cardiologia",
            "Dermatologia",
            "Oftalmologia",
            "Pediatria",
            "Clinica Geral",
            "Neurologia",
            "Ortopedia",
            "Gastroenterologia",
            "Pneumologia",
            "Psiquiatria"
    );

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Only seed if database is empty
        if (userRepository.count() > 0) {
            log.info("Database already populated, skipping seed");
            return;
        }

        log.info("Starting database seeding...");

        try {
            // Create admin user
            createAdminUser();

            // Create patient users
            List<String> patientIds = createPatients(5);

            // Create doctor users
            List<String> doctorIds = createDoctors(10);

            // Create appointments
            createAppointments(patientIds, doctorIds);

            log.info("Database seeding completed successfully!");
        } catch (Exception e) {
            log.error("Error during database seeding: ", e);
            // Continue anyway to allow app to start
        }
    }

    private void createAdminUser() {
        log.info("Creating admin user...");
        CreateUserDTO adminDTO = CreateUserDTO.builder()
                .name("Admin Consulta Fácil")
                .email("admin@consultafacil.com")
                .password("admin123")
                .cpf("12345678901")
                .phone("11999999999")
                .birthDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .gender(Gender.MALE)
                .build();

        userService.createUser(adminDTO);
        log.info("Admin user created: admin@consultafacil.com");
    }

    private List<String> createPatients(int count) {
        log.info("Creating {} patient users...", count);
        List<String> patientIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            CreateUserDTO patientDTO = CreateUserDTO.builder()
                    .name(faker.name().fullName())
                    .email(faker.internet().emailAddress())
                    .password("patient123")
                    .cpf(generateFakeCPF())
                    .phone(faker.phoneNumber().cellPhone())
                    .birthDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                    .gender(faker.bool().bool() ? Gender.MALE : Gender.FEMALE)
                    .build();

            var userResponse = userService.createUser(patientDTO);
            User user = userRepository.findById(userResponse.getId()).orElseThrow();

            // Create patient profile
            PatientProfile patientProfile = PatientProfile.builder()
                    .user(user)
                    .occupation(faker.job().title())
                    .build();

            PatientProfile savedProfile = patientProfileRepository.save(patientProfile);
            patientIds.add(savedProfile.getId());  // Add PatientProfile ID, not User ID
        }

        log.info("Created {} patient users", count);
        return patientIds;
    }

    private List<String> createDoctors(int count) {
        log.info("Creating {} doctor users...", count);
        List<String> doctorIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            CreateUserDTO doctorUserDTO = CreateUserDTO.builder()
                    .name(faker.name().fullName())
                    .email(faker.internet().emailAddress())
                    .password("doctor123")
                    .cpf(generateFakeCPF())
                    .phone(faker.phoneNumber().cellPhone())
                    .birthDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                    .gender(faker.bool().bool() ? Gender.MALE : Gender.FEMALE)
                    .build();

            var userResponse = userService.createUser(doctorUserDTO);

            CreateDoctorDTO doctorDTO = CreateDoctorDTO.builder()
                    .specialty(specialties.get(faker.random().nextInt(specialties.size())))
                    .licenseNumber("CRM" + System.currentTimeMillis() + faker.random().nextInt(10000))
                    .build();

            var doctorResponse = doctorService.createDoctorProfile(userResponse.getId(), doctorDTO);
            doctorIds.add(doctorResponse.getId());
        }

        log.info("Created {} doctor users", count);
        return doctorIds;
    }

    private void createAppointments(List<String> patientIds, List<String> doctorIds) {
        log.info("Creating appointments...");
        int appointmentCount = 0;

        for (String patientId : patientIds) {
            int appointmentsPerPatient = faker.random().nextInt(1, 5);

            for (int i = 0; i < appointmentsPerPatient; i++) {
                String doctorId = doctorIds.get(faker.random().nextInt(doctorIds.size()));
                LocalDateTime appointmentTime = LocalDateTime.now()
                        .plusDays(faker.random().nextInt(1, 60))
                        .withHour(faker.random().nextInt(8, 18))
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

                CreateAppointmentDTO appointmentDTO = CreateAppointmentDTO.builder()
                        .doctorId(doctorId)
                        .scheduledAt(appointmentTime)
                        .reason(faker.medical().medicineName())
                        .notes(faker.shakespeare().hamletQuote())
                        .build();

                try {
                    appointmentService.scheduleAppointment(patientId, appointmentDTO);
                    appointmentCount++;
                } catch (Exception e) {
                    log.debug("Error creating appointment: {}", e.getMessage());
                }
            }
        }

        log.info("Created {} appointments", appointmentCount);
    }

    private String generateFakeCPF() {
        StringBuilder cpf = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            cpf.append(faker.random().nextInt(0, 9));
        }
        return cpf.toString();
    }
}
