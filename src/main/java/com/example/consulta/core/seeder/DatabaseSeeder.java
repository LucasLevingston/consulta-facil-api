package com.example.consulta.core.seeder;

import com.example.consulta.application.service.AppointmentService;
import com.example.consulta.application.service.ClinicService;
import com.example.consulta.application.service.ProfessionalService;
import com.example.consulta.application.service.UserService;
import com.example.consulta.api.dto.appointment.CreateAppointmentDTO;
import com.example.consulta.api.dto.clinic.CreateClinicDTO;
import com.example.consulta.api.dto.professional.CreateProfessionalDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
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
@Profile("seed | (!prod & !test)")
public class DatabaseSeeder implements CommandLineRunner {

    private final Flyway flyway;
    private final PatientProfileRepository patientProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final UserService userService;
    private final ProfessionalService professionalService;
    private final AppointmentService appointmentService;
    private final ClinicService clinicService;

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

            String professionalProfileId = createProfessional(
                    "professional@example.com",
                    "12345678",
                    "Dr. Profissional Teste",
                    "00000000002",
                    "Cardiologia",
                    "CRM-TESTE-001");
            professionalService.approveApplication(professionalProfileId);

            String adminProfessionalProfileId = createProfessional(
                    "admin@example.com",
                    "12345678",
                    "Admin Teste",
                    "00000000003",
                    "Clinica Geral",
                    "CRM-ADMIN-001");
            professionalService.approveApplication(adminProfessionalProfileId);

            List<String> patientUserIds = createPatients(20);

            List<String> professionalProfileIds = createProfessionals(20);
            professionalProfileIds.forEach(id -> {
                try {
                    professionalService.approveApplication(id);
                } catch (Exception e) {
                    log.debug("Erro ao aprovar profissional {}: {}", id, e.getMessage());
                }
            });

            createClinics(professionalProfileId, adminProfessionalProfileId, professionalProfileIds);

            createAppointments(patientUserIds, professionalProfileIds);

            createTestAppointments(patientUserId, professionalProfileId);

            createBulkAppointmentsForTestProfessional(
                    professionalProfileId,
                    patientUserIds);

        } catch (Exception e) {
            log.error("Erro durante o seed:", e);
        }
    }

    private record CityLocation(String city, String state, double lat, double lng) {}

    private void createClinics(String testDoctorProfileId, String adminDoctorProfileId, List<String> extraDoctorProfileIds) {

        record ClinicDef(String name, String description, String phone, String address,
                         CityLocation location, String imageUrl, String ownerProfileId) {}

        List<CityLocation> cities = List.of(
                new CityLocation("São Paulo",      "SP", -23.5505, -46.6333),
                new CityLocation("Rio de Janeiro", "RJ", -22.9068, -43.1729),
                new CityLocation("Belo Horizonte", "MG", -19.9191, -43.9386),
                new CityLocation("Curitiba",       "PR", -25.4290, -49.2671),
                new CityLocation("Porto Alegre",   "RS", -30.0346, -51.2177),
                new CityLocation("Brasília",       "DF", -15.7942, -47.8822)
        );

        List<ClinicDef> defs = List.of(
                new ClinicDef(
                        "Clínica Cardio Saúde",
                        "Especializada em cardiologia e prevenção cardiovascular",
                        "(11) 3344-5566",
                        "Av. Paulista, 1578",
                        cities.get(0),
                        "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=600",
                        testDoctorProfileId),
                new ClinicDef(
                        "Instituto Carioca de Saúde",
                        "Atendimento multidisciplinar com foco em qualidade de vida",
                        "(21) 2233-4455",
                        "Rua Visconde de Pirajá, 330",
                        cities.get(1),
                        "https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=600",
                        adminDoctorProfileId),
                new ClinicDef(
                        "Centro Médico BH",
                        "Clínica geral e especialidades para toda a família",
                        "(31) 3344-7788",
                        "Av. Afonso Pena, 1000",
                        cities.get(2),
                        "https://images.unsplash.com/photo-1504813184591-01572f98c85f?w=600",
                        extraDoctorProfileIds.size() > 0 ? extraDoctorProfileIds.get(0) : testDoctorProfileId),
                new ClinicDef(
                        "Clínica Curitibana",
                        "Medicina preventiva e diagnóstico avançado",
                        "(41) 3344-9900",
                        "Rua XV de Novembro, 700",
                        cities.get(3),
                        "https://images.unsplash.com/photo-1530497610245-94d3c16cda28?w=600",
                        extraDoctorProfileIds.size() > 2 ? extraDoctorProfileIds.get(2) : testDoctorProfileId),
                new ClinicDef(
                        "Saúde Sul Clínica",
                        "Atendimento humanizado em Porto Alegre",
                        "(51) 3344-1122",
                        "Av. Independência, 500",
                        cities.get(4),
                        "https://images.unsplash.com/photo-1538108149393-fbbd81895907?w=600",
                        extraDoctorProfileIds.size() > 4 ? extraDoctorProfileIds.get(4) : testDoctorProfileId),
                new ClinicDef(
                        "Clínica Capital Federal",
                        "Excelência em saúde no coração do Brasil",
                        "(61) 3344-3344",
                        "SCS Quadra 2, Bloco C",
                        cities.get(5),
                        "https://images.unsplash.com/photo-1516549655169-df83a0774514?w=600",
                        extraDoctorProfileIds.size() > 6 ? extraDoctorProfileIds.get(6) : testDoctorProfileId)
        );

        for (ClinicDef def : defs) {
            try {
                // Set location on the owner professional profile
                professionalProfileRepository.findById(def.ownerProfileId()).ifPresent(p -> {
                    p.setCity(def.location().city());
                    p.setState(def.location().state());
                    p.setLatitude(def.location().lat() + (faker.random().nextDouble() * 0.02 - 0.01));
                    p.setLongitude(def.location().lng() + (faker.random().nextDouble() * 0.02 - 0.01));
                    professionalProfileRepository.save(p);
                });

                // Get the owning user id
                String ownerUserId = professionalProfileRepository.findById(def.ownerProfileId())
                        .map(p -> p.getUser().getId())
                        .orElse(null);

                if (ownerUserId == null) continue;

                CreateClinicDTO dto = new CreateClinicDTO();
                dto.setName(def.name());
                dto.setDescription(def.description());
                dto.setPhone(def.phone());
                dto.setAddress(def.address());
                dto.setCity(def.location().city());
                dto.setState(def.location().state());
                dto.setLatitude(def.location().lat());
                dto.setLongitude(def.location().lng());
                dto.setImageUrl(def.imageUrl());

                var clinic = clinicService.createClinic(ownerUserId, dto);

                // Add 1-2 extra doctors from the pool as members
                int added = 0;
                for (String extraId : extraDoctorProfileIds) {
                    if (added >= 2) break;
                    if (extraId.equals(def.ownerProfileId())) continue;
                    try {
                        clinicService.addMember(clinic.getId(), extraId, ownerUserId);

                        // Also give that doctor location data near the clinic
                        professionalProfileRepository.findById(extraId).ifPresent(p -> {
                            if (p.getLatitude() == null) {
                                p.setCity(def.location().city());
                                p.setState(def.location().state());
                                p.setLatitude(def.location().lat() + (faker.random().nextDouble() * 0.04 - 0.02));
                                p.setLongitude(def.location().lng() + (faker.random().nextDouble() * 0.04 - 0.02));
                                professionalProfileRepository.save(p);
                            }
                        });
                        added++;
                    } catch (Exception ignored) {}
                }

                log.info("Clínica criada: {} ({}, {})", clinic.getName(), def.location().city(), def.location().state());
            } catch (Exception e) {
                log.warn("Erro ao criar clínica {}: {}", def.name(), e.getMessage());
            }
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

    private List<String> createProfessionals(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                CreateUserDTO userDTO = CreateUserDTO.builder().name(faker.name().fullName())
                        .email(faker.internet().emailAddress()).password("prof123").cpf(generateFakeCPF())
                        .phone(faker.phoneNumber().cellPhone())
                        .birthDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        .gender(faker.bool().bool() ? Gender.MALE : Gender.FEMALE)
                        .imageUrl("https://i.pravatar.cc/150?img=" + faker.random().nextInt(1, 70)).build();
                var userResponse = userService.createUser(userDTO);
                CreateProfessionalDTO profDTO = CreateProfessionalDTO.builder()
                        .specialty(specialties.get(i % specialties.size()))
                        .licenseNumber("REG" + System.currentTimeMillis() + i).build();
                var profResponse = professionalService.createProfessionalProfile(userResponse.getId(), profDTO);
                ids.add(profResponse.getId());
            } catch (Exception e) {
                log.debug("Erro ao criar profissional fake: {}", e.getMessage());
            }
        }
        return ids;
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

    private String createProfessional(
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

        var profResponse = professionalService.createProfessionalProfile(
                userResponse.getId(),
                CreateProfessionalDTO.builder()
                        .specialty(specialty)
                        .licenseNumber(licenseNumber)
                        .build());

        return profResponse.getId();
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
            String testProfessionalProfileId) {

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
                                .doctorId(testProfessionalProfileId)
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

    private void createBulkAppointmentsForTestProfessional(
            String testProfessionalProfileId,
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
                                .doctorId(testProfessionalProfileId)
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
                "Criadas {} consultas para o profissional de teste",
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