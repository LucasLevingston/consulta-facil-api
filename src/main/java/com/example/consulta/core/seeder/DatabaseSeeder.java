package com.example.consulta.core.seeder;

import com.example.consulta.application.port.in.ProfessionalScheduleUseCase;
import com.example.consulta.application.service.AppointmentService;
import com.example.consulta.application.service.ClinicService;
import com.example.consulta.application.service.CreateProcedureRequestService;
import com.example.consulta.application.service.CreateProfessionalServiceService;
import com.example.consulta.application.service.InviteReceptionistService;
import com.example.consulta.application.service.ProfessionalService;
import com.example.consulta.application.service.SetConsultationPriceService;
import com.example.consulta.application.service.UserService;
import com.example.consulta.api.dto.appointment.CreateAppointmentDTO;
import com.example.consulta.api.dto.schedule.CreateProfessionalScheduleDTO;
import com.example.consulta.api.dto.clinic.CreateClinicDTO;
import com.example.consulta.api.dto.procedurerequest.CreateProcedureRequestDTO;
import com.example.consulta.api.dto.professional.CreateProfessionalDTO;
import com.example.consulta.api.dto.professionalservice.CreateProfessionalServiceDTO;
import com.example.consulta.api.dto.receptionist.InviteReceptionistDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.entity.ClinicalNote;
import com.example.consulta.domain.entity.Clinic;
import com.example.consulta.domain.entity.ClinicWorkingHours;
import com.example.consulta.domain.entity.ExamRequest;
import com.example.consulta.domain.entity.MedicalRecord;
import com.example.consulta.domain.entity.Notification;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.entity.Subscription;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.AppointmentModality;
import com.example.consulta.domain.enums.AppointmentPaymentStatus;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.ExamRequestStatus;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.enums.NotificationStatus;
import com.example.consulta.domain.enums.NotificationType;
import com.example.consulta.domain.enums.SubscriptionStatus;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.ClinicalNoteRepository;
import com.example.consulta.domain.repository.ClinicRepository;
import com.example.consulta.domain.repository.ClinicWorkingHoursRepository;
import com.example.consulta.domain.repository.ExamRequestRepository;
import com.example.consulta.domain.repository.MedicalRecordRepository;
import com.example.consulta.domain.repository.NotificationRepository;
import com.example.consulta.domain.repository.PatientProfileRepository;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import com.example.consulta.domain.repository.SubscriptionRepository;
import com.example.consulta.domain.repository.UserRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final InviteReceptionistService inviteReceptionistService;
    private final CreateProfessionalServiceService createProfessionalServiceService;
    private final SetConsultationPriceService setConsultationPriceService;
    private final CreateProcedureRequestService createProcedureRequestService;
    private final ProfessionalScheduleUseCase professionalScheduleUseCase;
    private final ClinicalNoteRepository clinicalNoteRepository;
    private final ClinicWorkingHoursRepository clinicWorkingHoursRepository;
    private final ExamRequestRepository examRequestRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;

    private final Faker faker = new Faker(new Locale("pt-BR"));

    private record ProfessionData(String profession, String specialty, String licensePrefix) {
    }

    private final List<ProfessionData> professionData = List.of(
            new ProfessionData("Médico", "Cardiologia", "CRM/SP"),
            new ProfessionData("Médico", "Clínica Geral", "CRM/SP"),
            new ProfessionData("Médico", "Dermatologia", "CRM/SP"),
            new ProfessionData("Médico", "Endocrinologia", "CRM/SP"),
            new ProfessionData("Médico", "Gastroenterologia", "CRM/SP"),
            new ProfessionData("Médico", "Neurologia", "CRM/SP"),
            new ProfessionData("Médico", "Oftalmologia", "CRM/SP"),
            new ProfessionData("Médico", "Ortopedia", "CRM/SP"),
            new ProfessionData("Médico", "Pediatria", "CRM/SP"),
            new ProfessionData("Médico", "Pneumologia", "CRM/SP"),
            new ProfessionData("Médico", "Psiquiatria", "CRM/SP"),
            new ProfessionData("Médico", "Cardiologia", "CRM/PB"),
            new ProfessionData("Médico", "Clínica Geral", "CRM/PB"),
            new ProfessionData("Médico", "Pediatria", "CRM/PB"),
            new ProfessionData("Médico", "Ginecologia", "CRM/PB"),
            new ProfessionData("Médico", "Ortopedia", "CRM/PB"),
            new ProfessionData("Médico", "Neurologia", "CRM/PB"),
            new ProfessionData("Médico", "Psiquiatria", "CRM/PB"),
            new ProfessionData("Nutricionista", "Nutrição Clínica", "CRN/SP"),
            new ProfessionData("Nutricionista", "Nutrição Esportiva", "CRN/SP"),
            new ProfessionData("Nutricionista", "Nutrição Oncológica", "CRN/SP"),
            new ProfessionData("Nutricionista", "Nutrição Materno-Infantil", "CRN/SP"),
            new ProfessionData("Nutricionista", "Nutrição Funcional", "CRN/SP"),
            new ProfessionData("Nutricionista", "Nutrição Clínica", "CRN/PB"),
            new ProfessionData("Nutricionista", "Nutrição Esportiva", "CRN/PB"),
            new ProfessionData("Fisioterapeuta", "Fisioterapia Ortopédica", "CREFITO/SP"),
            new ProfessionData("Fisioterapeuta", "Fisioterapia Neurológica", "CREFITO/SP"),
            new ProfessionData("Fisioterapeuta", "Fisioterapia Respiratória", "CREFITO/SP"),
            new ProfessionData("Fisioterapeuta", "Fisioterapia Desportiva", "CREFITO/SP"),
            new ProfessionData("Fisioterapeuta", "Pilates Clínico", "CREFITO/SP"),
            new ProfessionData("Fisioterapeuta", "Fisioterapia Ortopédica", "CREFITO/PB"),
            new ProfessionData("Fisioterapeuta", "Fisioterapia Neurológica", "CREFITO/PB"),
            new ProfessionData("Psicólogo", "TCC", "CRP/SP"),
            new ProfessionData("Psicólogo", "Psicanálise", "CRP/SP"),
            new ProfessionData("Psicólogo", "Psicologia Infantil", "CRP/SP"),
            new ProfessionData("Psicólogo", "Psicologia Organizacional", "CRP/SP"),
            new ProfessionData("Psicólogo", "Psicologia do Esporte", "CRP/SP"),
            new ProfessionData("Psicólogo", "TCC", "CRP/PB"),
            new ProfessionData("Psicólogo", "Psicanálise", "CRP/PB"),
            new ProfessionData("Dentista", "Odontologia Geral", "CRO/PB"),
            new ProfessionData("Dentista", "Ortodontia", "CRO/PB"),
            new ProfessionData("Dentista", "Implantodontia", "CRO/PB"));

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
                    "Profissional Teste",
                    "00000000002",
                    "Médico",
                    "Cardiologia",
                    "CRM-TESTE-001"

            );
            professionalService.approveApplication(professionalProfileId);

            String professionalUserId = professionalProfileRepository
                    .findById(professionalProfileId)
                    .map(p -> p.getUser().getId())
                    .orElse(null);

            seedServicesAndProcedureRequests(professionalUserId, patientUserId);
            seedSchedule(professionalUserId, ScheduleTemplate.FULL_WEEK);

            String adminProfessionalProfileId = createProfessional(
                    "admin@example.com",
                    "12345678",
                    "Admin Teste",
                    "00000000003",
                    "Médico",
                    "Clínica Geral",
                    "CRM-ADMIN-001");
            professionalService.approveApplication(adminProfessionalProfileId);

            List<String> patientUserIds = createPatients(20);

            List<String> professionalProfileIds = createProfessionals(52);
            professionalProfileIds.forEach(id -> {
                try {
                    professionalService.approveApplication(id);
                } catch (Exception e) {
                    log.debug("Erro ao aprovar profissional {}: {}", id, e.getMessage());
                }
            });
            seedSchedulesForProfessionals(professionalProfileIds);

            String firstClinicId = createClinics(professionalProfileId, adminProfessionalProfileId,
                    professionalProfileIds);

            String professionalOwnerUserId = professionalProfileRepository
                    .findById(professionalProfileId)
                    .map(p -> p.getUser().getId())
                    .orElse(null);

            if (firstClinicId != null && professionalOwnerUserId != null) {
                createReceptionist(
                        "receptionist@example.com",
                        "12345678",
                        "Recepcionista Teste",
                        "00000000004",
                        firstClinicId,
                        professionalOwnerUserId);
            }

            createAppointments(patientUserIds, professionalProfileIds);

            createTestAppointments(patientUserId, professionalProfileId);

            createBulkAppointmentsForTestProfessional(
                    professionalProfileId,
                    patientUserIds);

            seedMedicalRecords(patientUserIds);
            seedSubscriptions(professionalUserId, professionalProfileIds);
            seedClinicalNotesAndExamRequests();
            seedNotifications(patientUserIds, professionalProfileIds);
            seedClinicWorkingHours();

        } catch (Exception e) {
            log.error("Erro durante o seed:", e);
        }
    }

    private record CityLocation(String city, String state, double lat, double lng) {
    }

    private String createClinics(String testProfessionalProfileId, String adminProfessionalProfileId,
            List<String> extraProfessionalProfileIds) {

        record ClinicDef(String name, String description, String phone, String address,
                CityLocation location, String imageUrl, String ownerProfileId) {
        }

        List<CityLocation> cities = List.of(
                new CityLocation("São Paulo", "SP", -23.5505, -46.6333),
                new CityLocation("Rio de Janeiro", "RJ", -22.9068, -43.1729),
                new CityLocation("Belo Horizonte", "MG", -19.9191, -43.9386),
                new CityLocation("Curitiba", "PR", -25.4290, -49.2671),
                new CityLocation("Porto Alegre", "RS", -30.0346, -51.2177),
                new CityLocation("Brasília", "DF", -15.7942, -47.8822),
                new CityLocation("João Pessoa", "PB", -7.1195, -34.8450),
                new CityLocation("Campina Grande", "PB", -7.2306, -35.8811));

        List<ClinicDef> defs = List.of(
                new ClinicDef(
                        "Clínica Cardio Saúde",
                        "Especializada em cardiologia e prevenção cardiovascular",
                        "(11) 3344-5566",
                        "Av. Paulista, 1578",
                        cities.get(0),
                        "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=600",
                        testProfessionalProfileId),
                new ClinicDef(
                        "Instituto Carioca de Saúde",
                        "Atendimento multidisciplinar com foco em qualidade de vida",
                        "(21) 2233-4455",
                        "Rua Visconde de Pirajá, 330",
                        cities.get(1),
                        "https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=600",
                        adminProfessionalProfileId),
                new ClinicDef(
                        "Centro Médico BH",
                        "Clínica geral e especialidades para toda a família",
                        "(31) 3344-7788",
                        "Av. Afonso Pena, 1000",
                        cities.get(2),
                        "https://images.unsplash.com/photo-1504813184591-01572f98c85f?w=600",
                        pickOrFallback(extraProfessionalProfileIds, 0, testProfessionalProfileId)),
                new ClinicDef(
                        "Clínica Curitibana",
                        "Medicina preventiva e diagnóstico avançado",
                        "(41) 3344-9900",
                        "Rua XV de Novembro, 700",
                        cities.get(3),
                        "https://images.unsplash.com/photo-1530497610245-94d3c16cda28?w=600",
                        pickOrFallback(extraProfessionalProfileIds, 2, testProfessionalProfileId)),
                new ClinicDef(
                        "Saúde Sul Clínica",
                        "Atendimento humanizado em Porto Alegre",
                        "(51) 3344-1122",
                        "Av. Independência, 500",
                        cities.get(4),
                        "https://images.unsplash.com/photo-1538108149393-fbbd81895907?w=600",
                        pickOrFallback(extraProfessionalProfileIds, 4, testProfessionalProfileId)),
                new ClinicDef(
                        "Clínica Capital Federal",
                        "Excelência em saúde no coração do Brasil",
                        "(61) 3344-3344",
                        "SCS Quadra 2, Bloco C",
                        cities.get(5),
                        "https://images.unsplash.com/photo-1516549655169-df83a0774514?w=600",
                        pickOrFallback(extraProfessionalProfileIds, 6, testProfessionalProfileId)),
                new ClinicDef(
                        "Clínica Saúde João Pessoa",
                        "Atendimento completo em cardiologia, pediatria e clínica geral",
                        "(83) 3224-5566",
                        "Av. Epitácio Pessoa, 1234",
                        cities.get(6),
                        "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=600",
                        pickOrFallback(extraProfessionalProfileIds, 8, testProfessionalProfileId)),
                new ClinicDef(
                        "Centro de Saúde Paraibano",
                        "Medicina preventiva e especialidades para toda a família em João Pessoa",
                        "(83) 3311-7788",
                        "Rua Cardoso Vieira, 200 — Miramar",
                        cities.get(6),
                        "https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=600",
                        pickOrFallback(extraProfessionalProfileIds, 10, testProfessionalProfileId)),
                new ClinicDef(
                        "Clínica Campina Grande Saúde",
                        "Referência em saúde no Agreste paraibano",
                        "(83) 3322-4411",
                        "Av. Assis Chateaubriand, 500",
                        cities.get(7),
                        "https://images.unsplash.com/photo-1504813184591-01572f98c85f?w=600",
                        pickOrFallback(extraProfessionalProfileIds, 12, testProfessionalProfileId)),
                new ClinicDef(
                        "NutriVida João Pessoa",
                        "Nutrição e fisioterapia integradas para melhor qualidade de vida",
                        "(83) 3244-9900",
                        "Rua Padre Meira, 89 — Tambauzinho",
                        cities.get(6),
                        "https://images.unsplash.com/photo-1530497610245-94d3c16cda28?w=600",
                        pickOrFallback(extraProfessionalProfileIds, 14, testProfessionalProfileId)));

        String firstClinicId = null;

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

                if (ownerUserId == null)
                    continue;

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

                if (firstClinicId == null)
                    firstClinicId = clinic.getId();

                // Add 1-2 extra doctors from the pool as members
                int added = 0;
                for (String extraId : extraProfessionalProfileIds) {
                    if (added >= 2)
                        break;
                    if (extraId.equals(def.ownerProfileId()))
                        continue;
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
                    } catch (Exception ignored) {
                    }
                }

                log.info("Clínica criada: {} ({}, {})", clinic.getName(), def.location().city(),
                        def.location().state());
            } catch (Exception e) {
                log.warn("Erro ao criar clínica {}: {}", def.name(), e.getMessage());
            }
        }

        return firstClinicId;
    }

    private void createReceptionist(
            String email,
            String password,
            String name,
            String cpf,
            String clinicId,
            String ownerUserId) {
        try {
            createPatient(email, password, name, cpf, "https://i.pravatar.cc/150?img=5");
            InviteReceptionistDTO dto = new InviteReceptionistDTO();
            dto.setEmail(email);
            inviteReceptionistService.execute(clinicId, ownerUserId, dto);
            log.info("Recepcionista criada: {}", email);
        } catch (Exception e) {
            log.warn("Erro ao criar recepcionista: {}", e.getMessage());
        }
    }

    private void createAppointments(List<String> patientUserIds, List<String> professionalProfileIds) {
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
                String professionalId = professionalProfileIds
                        .get(faker.random().nextInt(professionalProfileIds.size()));
                AppointmentStatus status = statusPool.get(faker.random().nextInt(statusPool.size()));
                LocalDateTime scheduledAt = resolveScheduledAt(status);
                try {
                    LocalDateTime safeDate = LocalDateTime.now().plusDays(faker.random().nextInt(1, 20)).withHour(10)
                            .withMinute(0).withSecond(0).withNano(0);
                    var response = appointmentService.scheduleAppointment(userId,
                            CreateAppointmentDTO.builder().professionalId(professionalId).scheduledAt(safeDate)
                                    .reason(reasons.get(faker.random().nextInt(reasons.size())))
                                    .notes(faker.lorem().sentence(12)).build());
                    final AppointmentStatus finalStatus = status;
                    final LocalDateTime finalScheduledAt = scheduledAt;
                    appointmentRepository.findById(response.getId()).ifPresent(appointment -> {
                        appointment.setStatus(finalStatus);
                        appointment.setScheduledAt(finalScheduledAt);
                        if (finalStatus == AppointmentStatus.COMPLETED) {
                            enrichCompletedAppointment(appointment);
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
        int created = 0;
        for (int i = 0; created < count; i++) {
            ProfessionData pd = professionData.get(i % professionData.size());
            try {
                CreateUserDTO userDTO = CreateUserDTO.builder()
                        .name(faker.name().fullName())
                        .email(faker.internet().emailAddress())
                        .password("prof1234")
                        .cpf(generateFakeCPF())
                        .phone(faker.phoneNumber().cellPhone())
                        .birthDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        .gender(faker.bool().bool() ? Gender.MALE : Gender.FEMALE)
                        .imageUrl("https://i.pravatar.cc/150?img=" + faker.random().nextInt(1, 70))
                        .build();
                var userResponse = userService.createUser(userDTO);
                CreateProfessionalDTO profDTO = CreateProfessionalDTO.builder()
                        .profession(pd.profession())
                        .specialty(pd.specialty())
                        .licenseNumber(pd.licensePrefix() + " " + (100000 + created))
                        .build();
                var profResponse = professionalService.createProfessionalProfile(userResponse.getId(), profDTO);
                ids.add(profResponse.getId());
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar profissional fake: {}", e.getMessage());
                created++;
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
            String profession,
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
                        .profession(profession)
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
                                .professionalId(testProfessionalProfileId)
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

                final AppointmentStatus finalStatus = statuses[i];
                appointmentRepository.findById(response.getId()).ifPresent(appointment -> {
                    appointment.setStatus(finalStatus);
                    if (finalStatus == AppointmentStatus.COMPLETED) {
                        appointment.setPaymentStatus(AppointmentPaymentStatus.PAID);
                        appointment.setPaymentAmount(BigDecimal.valueOf(250));
                    }
                    appointmentRepository.save(appointment);
                });

            } catch (Exception e) {
                log.debug("Erro ao criar consulta de teste: {}", e.getMessage());
            }
        }

        // ONLINE appointments for frontend testing
        try {
            var onlineConfirmed = appointmentService.scheduleAppointment(
                    patientUserId,
                    CreateAppointmentDTO.builder()
                            .professionalId(testProfessionalProfileId)
                            .scheduledAt(LocalDateTime.now().plusDays(3).withHour(16).withMinute(0).withSecond(0)
                                    .withNano(0))
                            .reason("Teleconsulta — acompanhamento")
                            .notes("Consulta online de acompanhamento.")
                            .build());
            appointmentRepository.findById(onlineConfirmed.getId()).ifPresent(a -> {
                a.setStatus(AppointmentStatus.CONFIRMED);
                a.setModality(AppointmentModality.ONLINE);
                a.setMeetLink("https://meet.google.com/abc-defg-hij");
                appointmentRepository.save(a);
            });
        } catch (Exception e) {
            log.debug("Erro ao criar consulta online confirmada: {}", e.getMessage());
        }

        try {
            var onlinePending = appointmentService.scheduleAppointment(
                    patientUserId,
                    CreateAppointmentDTO.builder()
                            .professionalId(testProfessionalProfileId)
                            .scheduledAt(LocalDateTime.now().plusDays(12).withHour(9).withMinute(30).withSecond(0)
                                    .withNano(0))
                            .reason("Teleconsulta — primeira consulta")
                            .notes("Paciente solicita atendimento online.")
                            .build());
            appointmentRepository.findById(onlinePending.getId()).ifPresent(a -> {
                a.setModality(AppointmentModality.ONLINE);
                appointmentRepository.save(a);
            });
        } catch (Exception e) {
            log.debug("Erro ao criar consulta online pendente: {}", e.getMessage());
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

                LocalDateTime scheduledAt = resolveScheduledAt(status);

                LocalDateTime safeDate = LocalDateTime.now()
                        .plusDays(1)
                        .withHour(10)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

                var response = appointmentService.scheduleAppointment(
                        patientId,
                        CreateAppointmentDTO.builder()
                                .professionalId(testProfessionalProfileId)
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
                        enrichCompletedAppointment(appointment);
                    }

                    appointmentRepository.save(appointment);
                });

                created++;

            } catch (Exception e) {
                log.debug(
                        "Erro ao criar consulta fake para profissional teste: {}",
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

    private void seedServicesAndProcedureRequests(String professionalUserId, String patientUserId) {
        if (professionalUserId == null)
            return;

        try {
            setConsultationPriceService.execute(professionalUserId, new BigDecimal("250.00"));
        } catch (Exception e) {
            log.warn("Erro ao definir preço de consulta no seed: {}", e.getMessage());
        }

        record ServiceDef(String name, String description, BigDecimal price, int duration,
                boolean requiresConsultation) {
        }

        List<ServiceDef> services = List.of(
                new ServiceDef("Consulta de Cardiologia", "Consulta clínica de cardiologia", new BigDecimal("250.00"),
                        30, false),
                new ServiceDef("ECG - Eletrocardiograma", "Exame do ritmo cardíaco em repouso",
                        new BigDecimal("180.00"), 20, false),
                new ServiceDef("Holter 24h", "Monitoramento cardíaco contínuo de 24 horas", new BigDecimal("350.00"),
                        60, true),
                new ServiceDef("Ecocardiograma", "Ultrassom do coração com avaliação funcional",
                        new BigDecimal("450.00"), 60, true),
                new ServiceDef("MAPA", "Monitoramento ambulatorial da pressão arterial", new BigDecimal("320.00"), 45,
                        true));

        List<String> requiresConsultationServiceIds = new ArrayList<>();

        for (ServiceDef def : services) {
            try {
                var dto = CreateProfessionalServiceDTO.builder()
                        .name(def.name())
                        .description(def.description())
                        .price(def.price())
                        .durationMinutes(def.duration())
                        .requiresConsultation(def.requiresConsultation())
                        .build();
                var created = createProfessionalServiceService.execute(professionalUserId, dto);
                if (def.requiresConsultation())
                    requiresConsultationServiceIds.add(created.getId());
                log.info("Serviço criado no seed: {}", def.name());
            } catch (Exception e) {
                log.warn("Erro ao criar serviço no seed: {}", e.getMessage());
            }
        }

        patientProfileRepository.findByUserId(patientUserId).ifPresent(patientProfile -> {
            for (String serviceId : requiresConsultationServiceIds) {
                try {
                    var dto = CreateProcedureRequestDTO.builder()
                            .serviceId(serviceId)
                            .patientId(patientProfile.getId())
                            .notes("Paciente encaminhado para avaliação. Aguarda agendamento.")
                            .build();
                    createProcedureRequestService.execute(professionalUserId, dto);
                    log.info("Procedure request criado no seed para serviceId={}", serviceId);
                } catch (Exception e) {
                    log.warn("Erro ao criar procedure request no seed: {}", e.getMessage());
                }
            }
        });
    }

    private enum ScheduleTemplate {
        FULL_WEEK, MORNING_ONLY, AFTERNOON_ONLY, THREE_DAYS
    }

    private void seedSchedule(String userId, ScheduleTemplate template) {
        if (userId == null)
            return;
        try {
            List<CreateProfessionalScheduleDTO> dtos = buildScheduleDTOs(template);
            professionalScheduleUseCase.saveMySchedule(userId, dtos);
            log.info("[Seed] Schedule seeded for userId={} template={}", userId, template);
        } catch (Exception e) {
            log.warn("[Seed] Failed to seed schedule for userId={}: {}", userId, e.getMessage());
        }
    }

    private List<CreateProfessionalScheduleDTO> buildScheduleDTOs(ScheduleTemplate template) {
        record SlotDef(String day, String start, String end, int duration, int breakMin, boolean active) {
        }

        List<SlotDef> slots = switch (template) {
            case FULL_WEEK -> List.of(
                    new SlotDef("MONDAY", "08:00", "17:00", 30, 10, true),
                    new SlotDef("TUESDAY", "08:00", "17:00", 30, 10, true),
                    new SlotDef("WEDNESDAY", "08:00", "17:00", 30, 10, true),
                    new SlotDef("THURSDAY", "08:00", "17:00", 30, 10, true),
                    new SlotDef("FRIDAY", "08:00", "16:00", 30, 10, true),
                    new SlotDef("SATURDAY", "08:00", "12:00", 30, 0, false),
                    new SlotDef("SUNDAY", "08:00", "12:00", 30, 0, false));
            case MORNING_ONLY -> List.of(
                    new SlotDef("MONDAY", "07:00", "12:00", 45, 15, true),
                    new SlotDef("TUESDAY", "07:00", "12:00", 45, 15, true),
                    new SlotDef("WEDNESDAY", "07:00", "12:00", 45, 15, true),
                    new SlotDef("THURSDAY", "07:00", "12:00", 45, 15, true),
                    new SlotDef("FRIDAY", "07:00", "12:00", 45, 15, true),
                    new SlotDef("SATURDAY", "08:00", "12:00", 45, 0, false),
                    new SlotDef("SUNDAY", "08:00", "12:00", 45, 0, false));
            case AFTERNOON_ONLY -> List.of(
                    new SlotDef("MONDAY", "13:00", "18:00", 30, 10, true),
                    new SlotDef("TUESDAY", "13:00", "18:00", 30, 10, true),
                    new SlotDef("WEDNESDAY", "13:00", "18:00", 30, 10, true),
                    new SlotDef("THURSDAY", "13:00", "18:00", 30, 10, true),
                    new SlotDef("FRIDAY", "13:00", "17:30", 30, 10, true),
                    new SlotDef("SATURDAY", "08:00", "12:00", 30, 0, false),
                    new SlotDef("SUNDAY", "08:00", "12:00", 30, 0, false));
            case THREE_DAYS -> List.of(
                    new SlotDef("MONDAY", "08:00", "12:00", 60, 15, true),
                    new SlotDef("TUESDAY", "08:00", "12:00", 60, 15, false),
                    new SlotDef("WEDNESDAY", "14:00", "19:00", 60, 15, true),
                    new SlotDef("THURSDAY", "08:00", "12:00", 60, 15, false),
                    new SlotDef("FRIDAY", "14:00", "19:00", 60, 15, true),
                    new SlotDef("SATURDAY", "08:00", "12:00", 60, 0, false),
                    new SlotDef("SUNDAY", "08:00", "12:00", 60, 0, false));
        };

        return slots.stream().map(s -> CreateProfessionalScheduleDTO.builder()
                .dayOfWeek(s.day())
                .startTime(LocalTime.parse(s.start()))
                .endTime(LocalTime.parse(s.end()))
                .consultationDurationMinutes(s.duration())
                .breakBetweenConsultationsMinutes(s.breakMin())
                .isActive(s.active())
                .build()).toList();
    }

    private void seedSchedulesForProfessionals(List<String> professionalProfileIds) {
        ScheduleTemplate[] templates = ScheduleTemplate.values();
        int assigned = 0;
        for (int i = 0; i < professionalProfileIds.size(); i++) {
            String profId = professionalProfileIds.get(i);
            String userId = professionalProfileRepository.findById(profId)
                    .map(p -> p.getUser().getId())
                    .orElse(null);
            if (userId == null)
                continue;
            // Assign round-robin templates so there's variety; ~40% get a schedule
            if (i % 5 == 0 || i % 5 == 1 || i % 5 == 2) {
                seedSchedule(userId, templates[i % templates.length]);
                assigned++;
            }
        }
        log.info("[Seed] Schedules seeded for {}/{} random professionals", assigned, professionalProfileIds.size());
    }

    private String generateFakeCPF() {
        StringBuilder cpf = new StringBuilder();
        for (int i = 0; i < 11; i++)
            cpf.append(faker.random().nextInt(0, 9));
        return cpf.toString();
    }

    private LocalDateTime resolveScheduledAt(AppointmentStatus status) {
        int hour = faker.random().nextInt(8, 18);
        int minute = List.of(0, 30).get(faker.random().nextInt(2));
        return switch (status) {
            case COMPLETED -> LocalDateTime.now()
                    .minusDays(faker.random().nextInt(1, 180))
                    .withHour(hour).withMinute(minute).withSecond(0).withNano(0);
            case CONFIRMED -> LocalDateTime.now()
                    .plusDays(faker.random().nextInt(1, 15))
                    .withHour(hour).withMinute(minute).withSecond(0).withNano(0);
            case CANCELED -> LocalDateTime.now()
                    .minusDays(faker.random().nextInt(1, 60))
                    .withHour(hour).withMinute(minute).withSecond(0).withNano(0);
            default -> LocalDateTime.now()
                    .plusDays(faker.random().nextInt(5, 90))
                    .withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        };
    }

    private void enrichCompletedAppointment(com.example.consulta.domain.entity.Appointment appointment) {
        appointment.setNotes(faker.lorem().paragraph());
        if (faker.bool().bool()) {
            appointment.setRating(3 + faker.random().nextInt(3));
            appointment.setRatingComment(faker.lorem().sentence());
        }
        int roll = faker.random().nextInt(100);
        if (roll < 70) {
            appointment.setPaymentStatus(AppointmentPaymentStatus.PAID);
            appointment.setPaymentAmount(BigDecimal.valueOf(150 + faker.random().nextInt(351)));
        } else if (roll < 90) {
            appointment.setPaymentStatus(AppointmentPaymentStatus.PENDING_PAYMENT);
            appointment.setPaymentAmount(BigDecimal.valueOf(150 + faker.random().nextInt(351)));
        }
    }

    private String pickOrFallback(List<String> list, int index, String fallback) {
        return list.size() > index ? list.get(index) : fallback;
    }

    private void seedMedicalRecords(List<String> patientUserIds) {
        List<String> allergies = List.of("Penicilina", "Dipirona", "Látex", "Frutos do mar", "Nenhuma");
        List<String> medications = List.of("Losartana 50mg", "Metformina 850mg", "Omeprazol 20mg", "Nenhum");
        List<String> familyHistory = List.of(
                "Hipertensão arterial, diabetes tipo 2",
                "Cardiopatia isquêmica",
                "Sem histórico relevante",
                "Câncer de mama na mãe");
        List<String> pastHistory = List.of(
                "Apendicectomia em 2015",
                "Fratura de fêmur em 2018",
                "Sem cirurgias anteriores",
                "Pneumonia em 2020");

        int created = 0;
        for (String userId : patientUserIds) {
            try {
                patientProfileRepository.findByUserId(userId).ifPresent(profile -> {
                    if (medicalRecordRepository.findByPatientProfileId(profile.getId()).isPresent()) return;
                    medicalRecordRepository.save(MedicalRecord.builder()
                            .patientProfile(profile)
                            .allergies(allergies.get(faker.random().nextInt(allergies.size())))
                            .currentMedication(medications.get(faker.random().nextInt(medications.size())))
                            .familyMedicalHistory(familyHistory.get(faker.random().nextInt(familyHistory.size())))
                            .pastMedicalHistory(pastHistory.get(faker.random().nextInt(pastHistory.size())))
                            .privacyConsent(true)
                            .treatmentConsent(true)
                            .disclosureConsent(faker.bool().bool())
                            .build());
                });
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar medical record: {}", e.getMessage());
            }
        }
        log.info("[Seed] MedicalRecords criados: {}", created);
    }

    private void seedSubscriptions(String testProfessionalUserId, List<String> professionalProfileIds) {
        List<String> planIds = List.of("plan_basic", "plan_pro", "plan_premium");
        List<SubscriptionStatus> statuses = List.of(
                SubscriptionStatus.ACTIVE, SubscriptionStatus.ACTIVE, SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PENDING, SubscriptionStatus.CANCELLED);

        // Subscription for test professional
        try {
            userRepository.findById(testProfessionalUserId).ifPresent(user -> {
                if (subscriptionRepository.findByUserId(user.getId()).isPresent()) return;
                subscriptionRepository.save(Subscription.builder()
                        .user(user)
                        .planId("plan_pro")
                        .status(SubscriptionStatus.ACTIVE)
                        .expiresAt(LocalDateTime.now().plusMonths(6))
                        .build());
            });
        } catch (Exception e) {
            log.debug("Erro ao criar subscription teste: {}", e.getMessage());
        }

        int created = 0;
        for (int i = 0; i < Math.min(professionalProfileIds.size(), 20); i++) {
            final String profId = professionalProfileIds.get(i);
            try {
                professionalProfileRepository.findById(profId).ifPresent(prof -> {
                    String userId = prof.getUser().getId();
                    if (subscriptionRepository.findByUserId(userId).isPresent()) return;
                    SubscriptionStatus status = statuses.get(faker.random().nextInt(statuses.size()));
                    subscriptionRepository.save(Subscription.builder()
                            .user(prof.getUser())
                            .planId(planIds.get(faker.random().nextInt(planIds.size())))
                            .status(status)
                            .expiresAt(status == SubscriptionStatus.ACTIVE
                                    ? LocalDateTime.now().plusMonths(faker.random().nextInt(1, 12))
                                    : null)
                            .build());
                });
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar subscription: {}", e.getMessage());
            }
        }
        log.info("[Seed] Subscriptions criadas: {}", created);
    }

    private void seedClinicalNotesAndExamRequests() {
        List<String> diagnoses = List.of("Hipertensão arterial", "Diabetes mellitus tipo 2",
                "Lombalgia crônica", "Ansiedade generalizada", "Gastrite crônica");
        List<String> cids = List.of("I10", "E11", "M54.5", "F41.1", "K29.5");
        List<String> exams = List.of("Hemograma completo", "Glicemia em jejum", "ECG",
                "Raio-X de tórax", "Ultrassom abdominal", "TSH", "Colesterol total");
        List<String> prescriptions = List.of(
                "Losartana 50mg 1x/dia", "Metformina 850mg 2x/dia",
                "Omeprazol 20mg em jejum", "Paracetamol 750mg se necessário");

        int notes = 0, examsCreated = 0;
        var completedAppointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .toList();

        for (var appointment : completedAppointments) {
            try {
                if (clinicalNoteRepository.findByAppointmentId(appointment.getId()).isEmpty()) {
                    int idx = faker.random().nextInt(diagnoses.size());
                    clinicalNoteRepository.save(ClinicalNote.builder()
                            .appointment(appointment)
                            .clinicalNotes(faker.lorem().paragraph(2))
                            .diagnosis(diagnoses.get(idx))
                            .diagnosisCid(cids.get(idx))
                            .prescription(prescriptions.get(faker.random().nextInt(prescriptions.size())))
                            .treatmentPlan(faker.lorem().sentence(10))
                            .followUpInstructions("Retornar em " + faker.random().nextInt(1, 6) + " meses.")
                            .build());
                    notes++;
                }
            } catch (Exception e) {
                log.debug("Erro ao criar clinical note: {}", e.getMessage());
            }

            if (faker.random().nextInt(100) < 40) {
                int count = faker.random().nextInt(1, 4);
                for (int i = 0; i < count; i++) {
                    try {
                        ExamRequestStatus examStatus = faker.random().nextInt(100) < 60
                                ? ExamRequestStatus.UPLOADED : ExamRequestStatus.PENDING;
                        examRequestRepository.save(ExamRequest.builder()
                                .appointment(appointment)
                                .professional(appointment.getProfessional())
                                .patient(appointment.getPatient())
                                .examName(exams.get(faker.random().nextInt(exams.size())))
                                .instructions("Realizar em jejum de 8 horas.")
                                .status(examStatus)
                                .professionalNotes(faker.lorem().sentence())
                                .build());
                        examsCreated++;
                    } catch (Exception e) {
                        log.debug("Erro ao criar exam request: {}", e.getMessage());
                    }
                }
            }
        }
        log.info("[Seed] ClinicalNotes: {}, ExamRequests: {}", notes, examsCreated);
    }

    private void seedNotifications(List<String> patientUserIds, List<String> professionalProfileIds) {
        record NotifTemplate(NotificationType type, String title, String message) {}

        List<NotifTemplate> templates = List.of(
                new NotifTemplate(NotificationType.APPOINTMENT_SCHEDULED,
                        "Consulta agendada", "Sua consulta foi agendada com sucesso."),
                new NotifTemplate(NotificationType.APPOINTMENT_CONFIRMED,
                        "Consulta confirmada", "Sua consulta foi confirmada pelo profissional."),
                new NotifTemplate(NotificationType.APPOINTMENT_CANCELED,
                        "Consulta cancelada", "Sua consulta foi cancelada."),
                new NotifTemplate(NotificationType.GENERAL,
                        "Resultado disponível", "O resultado do seu exame está disponível.")
        );

        List<NotificationStatus> statuses = List.of(
                NotificationStatus.READ, NotificationStatus.READ,
                NotificationStatus.PENDING, NotificationStatus.PENDING);

        int created = 0;
        for (String userId : patientUserIds) {
            int count = faker.random().nextInt(2, 6);
            for (int i = 0; i < count; i++) {
                try {
                    userRepository.findById(userId).ifPresent(user -> {
                        NotifTemplate t = templates.get(faker.random().nextInt(templates.size()));
                        notificationRepository.save(Notification.builder()
                                .targetUser(user)
                                .type(t.type())
                                .title(t.title())
                                .message(t.message())
                                .status(statuses.get(faker.random().nextInt(statuses.size())))
                                .build());
                    });
                    created++;
                } catch (Exception e) {
                    log.debug("Erro ao criar notification: {}", e.getMessage());
                }
            }
        }
        log.info("[Seed] Notifications criadas: {}", created);
    }

    private void seedClinicWorkingHours() {
        record DaySlot(String day, LocalTime open, LocalTime close, boolean isOpen) {}
        List<DaySlot> slots = List.of(
                new DaySlot("MONDAY",    LocalTime.of(8,  0), LocalTime.of(18, 0), true),
                new DaySlot("TUESDAY",   LocalTime.of(8,  0), LocalTime.of(18, 0), true),
                new DaySlot("WEDNESDAY", LocalTime.of(8,  0), LocalTime.of(18, 0), true),
                new DaySlot("THURSDAY",  LocalTime.of(8,  0), LocalTime.of(18, 0), true),
                new DaySlot("FRIDAY",    LocalTime.of(8,  0), LocalTime.of(17, 0), true),
                new DaySlot("SATURDAY",  LocalTime.of(8,  0), LocalTime.of(12, 0), false),
                new DaySlot("SUNDAY",    LocalTime.of(8,  0), LocalTime.of(12, 0), false)
        );

        int created = 0;
        for (Clinic clinic : clinicRepository.findAll()) {
            for (DaySlot slot : slots) {
                try {
                    if (clinicWorkingHoursRepository.findByClinicIdAndDayOfWeek(clinic.getId(), slot.day()).isEmpty()) {
                        clinicWorkingHoursRepository.save(ClinicWorkingHours.builder()
                                .clinic(clinic)
                                .dayOfWeek(slot.day())
                                .openTime(slot.open())
                                .closeTime(slot.close())
                                .isOpen(slot.isOpen())
                                .build());
                        created++;
                    }
                } catch (Exception e) {
                    log.debug("Erro ao criar working hours: {}", e.getMessage());
                }
            }
        }
        log.info("[Seed] ClinicWorkingHours criados: {}", created);
    }
}
