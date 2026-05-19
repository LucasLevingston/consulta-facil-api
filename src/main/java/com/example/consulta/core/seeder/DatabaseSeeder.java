package com.example.consulta.core.seeder;

import com.example.consulta.application.service.AppointmentService;
import com.example.consulta.application.service.DoctorService;
import com.example.consulta.application.service.UserService;
import com.example.consulta.api.dto.appointment.CreateAppointmentDTO;
import com.example.consulta.api.dto.doctor.CreateDoctorDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.PatientProfileRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!prod & !test")
public class DatabaseSeeder implements CommandLineRunner {

    private final Flyway flyway;
    private final PatientProfileRepository patientProfileRepository;
    private final AppointmentRepository appointmentRepository;
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
            "Psiquiatria");

    @Override
    public void run(String... args) {

        flyway.clean();
        flyway.migrate();

        try {

            String patientUserId = createPatient(
                    "patient@example.com",
                    "12345678",
                    "Paciente Teste",
                    "00000000001",
                    "https://i.pravatar.cc/150?img=1");

            String doctorProfileId = createDoctor(
                    "doctor@example.com",
                    "12345678",
                    "Dr. Doutor Teste",
                    "00000000002",
                    "Cardiologia",
                    "CRM-TESTE-001");
            doctorService.approveDoctorApplication(doctorProfileId);

            String adminDoctorProfileId = createDoctor(
                    "admin@example.com",
                    "12345678",
                    "Admin Teste",
                    "00000000003",
                    "Clinica Geral",
                    "CRM-ADMIN-001");
            doctorService.approveDoctorApplication(adminDoctorProfileId);

            List<String> patientUserIds = createPatients(20);

            List<String> doctorProfileIds = createDoctors(20);
            doctorProfileIds.forEach(id -> {
                try {
                    doctorService.approveDoctorApplication(id);
                } catch (Exception e) {
                    log.debug("Erro ao aprovar médico {}: {}", id, e.getMessage());
                }
            });

            createAppointments(patientUserIds, doctorProfileIds);

            createTestAppointments(patientUserId, doctorProfileId);

            createBulkAppointmentsForTestDoctor(
                    doctorProfileId,
                    patientUserIds);

        } catch (Exception e) {
            log.error("Erro durante o seed:", e);
        }
    }

    private void createAppointments(List<String> patientUserIds, List<String> doctorProfileIds) {
        List<AppointmentStatus> statusPool = List.of(AppointmentStatus.COMPLETED, AppointmentStatus.COMPLETED,
                AppointmentStatus.COMPLETED, AppointmentStatus.CONFIRMED, AppointmentStatus.CONFIRMED,
                AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING, AppointmentStatus.PENDING,
                AppointmentStatus.CANCELED);
        List<String> reasons = List.of("Consulta de rotina", "Retorno médico", "Dor de cabeça frequente",
                "Check-up anual", "Avaliação clínica", "Exames laboratoriais", "Acompanhamento psicológico",
                "Dor nas costas", "Febre persistente", "Pressão alta", "Ansiedade", "Consulta preventiva",
                "Avaliação cardíaca", "Problemas respiratórios");
        int totalAppointments = 0;
        for (String userId : patientUserIds) {
            int appointmentsPerPatient = faker.random().nextInt(5, 13);
            for (int i = 0; i < appointmentsPerPatient; i++) {
                String doctorId = doctorProfileIds.get(faker.random().nextInt(doctorProfileIds.size()));
                AppointmentStatus status = statusPool.get(faker.random().nextInt(statusPool.size()));
                LocalDateTime scheduledAt;
                switch (status) {
                    case COMPLETED -> scheduledAt = LocalDateTime.now().minusDays(faker.random().nextInt(1, 180))
                            .withHour(faker.random().nextInt(8, 18))
                            .withMinute(List.of(0, 30).get(faker.random().nextInt(2))).withSecond(0).withNano(0);
                    case CANCELED -> {
                        boolean futureCanceled = faker.bool().bool();
                        scheduledAt = futureCanceled ? LocalDateTime.now().plusDays(faker.random().nextInt(1, 30))
                                : LocalDateTime.now().minusDays(faker.random().nextInt(1, 60));
                        scheduledAt = scheduledAt.withHour(faker.random().nextInt(8, 18))
                                .withMinute(List.of(0, 30).get(faker.random().nextInt(2))).withSecond(0).withNano(0);
                    }
                    case CONFIRMED -> scheduledAt = LocalDateTime.now().plusDays(faker.random().nextInt(1, 15))
                            .withHour(faker.random().nextInt(8, 18))
                            .withMinute(List.of(0, 30).get(faker.random().nextInt(2))).withSecond(0).withNano(0);
                    default -> scheduledAt = LocalDateTime.now().plusDays(faker.random().nextInt(5, 90))
                            .withHour(faker.random().nextInt(8, 18))
                            .withMinute(List.of(0, 30).get(faker.random().nextInt(2))).withSecond(0).withNano(0);
                }
                try {
                    LocalDateTime safeDate = LocalDateTime.now().plusDays(faker.random().nextInt(1, 20)).withHour(10)
                            .withMinute(0).withSecond(0).withNano(0);
                    var response = appointmentService.scheduleAppointment(userId,
                            CreateAppointmentDTO.builder().doctorId(doctorId).scheduledAt(safeDate)
                                    .reason(reasons.get(faker.random().nextInt(reasons.size())))
                                    .notes(faker.lorem().sentence(12)).build());
                    final AppointmentStatus finalStatus = status;
                    final LocalDateTime finalScheduledAt = scheduledAt;
                    appointmentRepository.findById(response.getId()).ifPresent(appointment -> {
                        appointment.setStatus(finalStatus);
                        appointment.setScheduledAt(finalScheduledAt);
                        if (finalStatus == AppointmentStatus.COMPLETED) {
                            appointment.setNotes(faker.lorem().paragraph());
                            if (faker.bool().bool()) {
                                appointment.setRating(3 + faker.random().nextInt(3));
                                appointment.setRatingComment(faker.lorem().sentence());
                            }
                        }
                        appointmentRepository.save(appointment);
                    });
                    totalAppointments++;
                } catch (Exception e) {
                    log.debug("Erro ao criar consulta fake: {}", e.getMessage());
                }
            }
        }
        log.info("Total de consultas criadas: {}", totalAppointments);
    }

    private List<String> createDoctors(int count) {
        List<String> doctorProfileIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                CreateUserDTO doctorUserDTO = CreateUserDTO.builder().name(faker.name().fullName())
                        .email(faker.internet().emailAddress()).password("doctor123").cpf(generateFakeCPF())
                        .phone(faker.phoneNumber().cellPhone())
                        .birthDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        .gender(faker.bool().bool() ? Gender.MALE : Gender.FEMALE)
                        .imageUrl("https://i.pravatar.cc/150?img=" + faker.random().nextInt(1, 70)).build();
                var userResponse = userService.createUser(doctorUserDTO);
                CreateDoctorDTO doctorDTO = CreateDoctorDTO.builder().specialty(specialties.get(i % specialties.size()))
                        .licenseNumber("CRM" + System.currentTimeMillis() + i).build();
                var doctorResponse = doctorService.createDoctorProfile(userResponse.getId(), doctorDTO);
                doctorProfileIds.add(doctorResponse.getId());
            } catch (Exception e) {
                log.debug("Erro ao criar médico fake: {}", e.getMessage());
            }
        }
        return doctorProfileIds;
    }

    private String createPatient(
            String email,
            String password,
            String name,
            String cpf,
            String imageUrl) {

        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name)
                .email(email)
                .password(password)
                .cpf(cpf)
                .phone("11900000001")
                .birthDate(LocalDate.of(1990, 1, 15))
                .gender(Gender.MALE)
                .imageUrl(imageUrl)
                .build();

        var created = userService.createUser(dto);

        return created.getId();
    }

    private String createDoctor(
            String email,
            String password,
            String name,
            String cpf,
            String specialty,
            String licenseNumber) {

        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name)
                .email(email)
                .password(password)
                .cpf(cpf)
                .phone("11900000002")
                .birthDate(LocalDate.of(1985, 6, 20))
                .imageUrl("https://i.pravatar.cc/150?img=" + faker.random().nextInt(1, 70))
                .gender(Gender.MALE)
                .build();

        var userResponse = userService.createUser(dto);

        var doctorResponse = doctorService.createDoctorProfile(
                userResponse.getId(),
                CreateDoctorDTO.builder()
                        .specialty(specialty)
                        .licenseNumber(licenseNumber)
                        .build());

        return doctorResponse.getId();
    }

    private void forceStatus(
            String appointmentId,
            AppointmentStatus status) {

        appointmentRepository.findById(appointmentId).ifPresent(appointment -> {
            appointment.setStatus(status);
            appointmentRepository.save(appointment);
        });
    }

    private void createTestAppointments(
            String patientUserId,
            String testDoctorProfileId) {

        PatientProfile profile = patientProfileRepository
                .findByUserId(patientUserId)
                .orElse(null);

        if (profile == null) {
            return;
        }

        String[] reasons = {
                "Consulta de rotina",
                "Dor no peito",
                "Check-up anual",
                "Pressão alta",
                "Retorno"
        };

        int[] daysAhead = { -10, -5, 1, 7, 20 };

        int[] hours = { 9, 11, 14, 10, 15 };

        AppointmentStatus[] statuses = {
                AppointmentStatus.COMPLETED,
                AppointmentStatus.COMPLETED,
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.PENDING,
                AppointmentStatus.PENDING
        };

        for (int i = 0; i < reasons.length; i++) {

            try {

                var response = appointmentService.scheduleAppointment(
                        patientUserId,
                        CreateAppointmentDTO.builder()
                                .doctorId(testDoctorProfileId)
                                .scheduledAt(
                                        LocalDateTime.now()
                                                .plusDays(daysAhead[i])
                                                .withHour(hours[i])
                                                .withMinute(0)
                                                .withSecond(0)
                                                .withNano(0))
                                .reason(reasons[i])
                                .notes(faker.lorem().sentence())
                                .build());

                forceStatus(response.getId(), statuses[i]);

            } catch (Exception e) {
                log.debug("Erro ao criar consulta de teste: {}", e.getMessage());
            }
        }
    }

    private void createBulkAppointmentsForTestDoctor(
            String testDoctorProfileId,
            List<String> patientUserIds) {

        List<AppointmentStatus> statuses = List.of(
                AppointmentStatus.COMPLETED,
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.PENDING,
                AppointmentStatus.CANCELED);

        List<String> reasons = List.of(
                "Consulta de rotina",
                "Check-up anual",
                "Dor de cabeça",
                "Pressão alta",
                "Retorno médico",
                "Exames laboratoriais",
                "Avaliação clínica",
                "Consulta preventiva");

        int created = 0;

        for (int i = 0; i < 100; i++) {

            try {

                String patientId = patientUserIds.get(
                        faker.random().nextInt(patientUserIds.size()));

                AppointmentStatus status = statuses.get(
                        faker.random().nextInt(statuses.size()));

                LocalDateTime scheduledAt;

                switch (status) {

                    case COMPLETED -> scheduledAt = LocalDateTime.now()
                            .minusDays(faker.random().nextInt(1, 120))
                            .withHour(faker.random().nextInt(8, 18))
                            .withMinute(List.of(0, 30).get(faker.random().nextInt(2)))
                            .withSecond(0)
                            .withNano(0);

                    case CONFIRMED -> scheduledAt = LocalDateTime.now()
                            .plusDays(faker.random().nextInt(1, 15))
                            .withHour(faker.random().nextInt(8, 18))
                            .withMinute(List.of(0, 30).get(faker.random().nextInt(2)))
                            .withSecond(0)
                            .withNano(0);

                    case CANCELED -> scheduledAt = LocalDateTime.now()
                            .minusDays(faker.random().nextInt(1, 30))
                            .withHour(faker.random().nextInt(8, 18))
                            .withMinute(List.of(0, 30).get(faker.random().nextInt(2)))
                            .withSecond(0)
                            .withNano(0);

                    default -> scheduledAt = LocalDateTime.now()
                            .plusDays(faker.random().nextInt(5, 60))
                            .withHour(faker.random().nextInt(8, 18))
                            .withMinute(List.of(0, 30).get(faker.random().nextInt(2)))
                            .withSecond(0)
                            .withNano(0);
                }

                LocalDateTime safeDate = LocalDateTime.now()
                        .plusDays(1)
                        .withHour(10)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

                var response = appointmentService.scheduleAppointment(
                        patientId,
                        CreateAppointmentDTO.builder()
                                .doctorId(testDoctorProfileId)
                                .scheduledAt(safeDate)
                                .reason(reasons.get(
                                        faker.random().nextInt(reasons.size())))
                                .notes(faker.lorem().sentence(10))
                                .build());

                final AppointmentStatus finalStatus = status;
                final LocalDateTime finalScheduledAt = scheduledAt;

                appointmentRepository.findById(response.getId()).ifPresent(appointment -> {

                    appointment.setStatus(finalStatus);

                    appointment.setScheduledAt(finalScheduledAt);

                    if (finalStatus == AppointmentStatus.COMPLETED) {
                        appointment.setNotes(faker.lorem().paragraph());
                        if (faker.bool().bool()) {
                            appointment.setRating(3 + faker.random().nextInt(3));
                            appointment.setRatingComment(faker.lorem().sentence());
                        }
                    }

                    appointmentRepository.save(appointment);
                });

                created++;

            } catch (Exception e) {
                log.debug(
                        "Erro ao criar consulta fake para médico teste: {}",
                        e.getMessage());
            }
        }

        log.info(
                "Criadas {} consultas para o médico de teste",
                created);
    }

    private List<String> createPatients(int count) {

        List<String> userIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            try {

                CreateUserDTO patientDTO = CreateUserDTO.builder()
                        .name(faker.name().fullName())
                        .email(faker.internet().emailAddress())
                        .password("patient123")
                        .cpf(generateFakeCPF())
                        .phone(faker.phoneNumber().cellPhone())
                        .birthDate(
                                faker.date()
                                        .birthday()
                                        .toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate())
                        .gender(faker.bool().bool()
                                ? Gender.MALE
                                : Gender.FEMALE)
                        .imageUrl("https://i.pravatar.cc/150?img=" + faker.random().nextInt(1, 70))
                        .build();

                var userResponse = userService.createUser(patientDTO);

                patientProfileRepository
                        .findByUserId(userResponse.getId())
                        .ifPresent(profile -> {
                            profile.setOccupation(faker.job().title());
                            patientProfileRepository.save(profile);
                        });

                userIds.add(userResponse.getId());

            } catch (Exception e) {
                log.debug("Erro ao criar paciente fake: {}", e.getMessage());
            }
        }

        return userIds;
    }

    private String generateFakeCPF() {
        StringBuilder cpf = new StringBuilder();
        for (int i = 0; i < 11; i++)
            cpf.append(faker.random().nextInt(0, 9));
        return cpf.toString();
    }
}