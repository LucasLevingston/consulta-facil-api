package com.consultafacil.core.seeder;

import com.consultafacil.application.port.in.ProfessionalScheduleUseCase;
import com.consultafacil.application.service.AppointmentService;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.entity.ExamLab;
import com.consultafacil.domain.entity.ExamLabHours;
import com.consultafacil.domain.entity.Invoice;
import com.consultafacil.domain.repository.BillingPaymentRepository;
import com.consultafacil.domain.repository.ExamLabHoursRepository;
import com.consultafacil.domain.repository.ExamLabRepository;
import com.consultafacil.domain.repository.InvoiceRepository;
import com.consultafacil.application.service.ClinicService;
import com.consultafacil.application.service.CreateProcedureRequestService;
import com.consultafacil.application.service.CreateProfessionalServiceService;
import com.consultafacil.application.service.InviteReceptionistService;
import com.consultafacil.application.service.ProfessionalService;
import com.consultafacil.application.service.SetConsultationPriceService;
import com.consultafacil.application.service.UserService;
import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.api.dto.schedule.CreateProfessionalScheduleDTO;
import com.consultafacil.api.dto.clinic.CreateClinicDTO;
import com.consultafacil.api.dto.procedurerequest.CreateProcedureRequestDTO;
import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.professionalservice.CreateProfessionalServiceDTO;
import com.consultafacil.api.dto.receptionist.InviteReceptionistDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.Address;
import com.consultafacil.domain.entity.ClinicalNote;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.ClinicWorkingHours;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.entity.CouponUse;
import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.entity.ExamRequest;
import com.consultafacil.domain.entity.MedicalHistory;
import com.consultafacil.domain.entity.MedicalRecord;
import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.SubscriptionPayment;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.OwnerType;
import com.consultafacil.domain.enums.PaymentType;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.ExamType;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.NotificationStatus;
import com.consultafacil.domain.enums.NotificationType;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.AddressRepository;
import com.consultafacil.domain.repository.AppointmentRepository;
import com.consultafacil.domain.repository.ClinicalNoteRepository;
import com.consultafacil.domain.repository.ClinicRepository;
import com.consultafacil.domain.repository.ClinicWorkingHoursRepository;
import com.consultafacil.domain.repository.CouponRepository;
import com.consultafacil.domain.repository.CouponUseRepository;
import com.consultafacil.domain.repository.EmergencyContactRepository;
import com.consultafacil.domain.repository.ExamRequestRepository;
import com.consultafacil.domain.repository.MedicalHistoryRepository;
import com.consultafacil.domain.repository.MedicalRecordRepository;
import com.consultafacil.domain.repository.NotificationRepository;
import com.consultafacil.domain.repository.PatientProfileRepository;
import com.consultafacil.domain.repository.PlanRepository;
import com.consultafacil.domain.repository.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.SellerRepository;
import com.consultafacil.domain.repository.SellerSaleRepository;
import com.consultafacil.domain.repository.SubscriptionPaymentRepository;
import com.consultafacil.domain.repository.SubscriptionRepository;
import com.consultafacil.domain.repository.UserRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@Profile("seed | (!prod & !test & !railway)")
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
    private final AddressRepository addressRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PlanRepository planRepository;
    private final SellerRepository sellerRepository;
    private final SellerSaleRepository sellerSaleRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final CouponRepository couponRepository;
    private final CouponUseRepository couponUseRepository;
    private final ExamLabRepository examLabRepository;
    private final ExamLabHoursRepository examLabHoursRepository;
    private final BillingPaymentRepository billingPaymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PasswordEncoder passwordEncoder;

    private final Faker faker = new Faker(new Locale("pt-BR"));

    private record ProfessionData(ProfessionalType profession, Specialty specialty, String licensePrefix) {
    }

    private final List<ProfessionData> professionData = List.of(
            new ProfessionData(ProfessionalType.MEDICO, Specialty.CARDIOLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.CLINICA_GERAL, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.DERMATOLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.ENDOCRINOLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.GASTROENTEROLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.NEUROLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.OFTALMOLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.ORTOPEDIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.PEDIATRIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.PNEUMOLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.PSIQUIATRIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.CARDIOLOGIA, "CRM/PB"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.CLINICA_GERAL, "CRM/PB"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.PEDIATRIA, "CRM/PB"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.GINECOLOGIA_OBSTETRICIA, "CRM/PB"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.ORTOPEDIA, "CRM/PB"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.NEUROLOGIA, "CRM/PB"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.PSIQUIATRIA, "CRM/PB"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_CLINICA, "CRN/SP"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_ESPORTIVA, "CRN/SP"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_ONCOLOGICA, "CRN/SP"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_MATERNO_INFANTIL, "CRN/SP"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_FUNCIONAL, "CRN/SP"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_CLINICA, "CRN/PB"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_ESPORTIVA, "CRN/PB"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.FISIOTERAPIA_ORTOPEDICA, "CREFITO/SP"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.FISIOTERAPIA_NEUROLOGICA, "CREFITO/SP"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.FISIOTERAPIA_RESPIRATORIA, "CREFITO/SP"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.FISIOTERAPIA_DESPORTIVA, "CREFITO/SP"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.PILATES_CLINICO, "CREFITO/SP"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.FISIOTERAPIA_ORTOPEDICA, "CREFITO/PB"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.FISIOTERAPIA_NEUROLOGICA, "CREFITO/PB"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.TCC, "CRP/SP"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.PSICANALISE, "CRP/SP"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.PSICOLOGIA_INFANTIL, "CRP/SP"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.PSICOLOGIA_ORGANIZACIONAL, "CRP/SP"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.PSICOLOGIA_DO_ESPORTE, "CRP/SP"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.TCC, "CRP/PB"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.PSICANALISE, "CRP/PB"),
            new ProfessionData(ProfessionalType.DENTISTA, Specialty.ODONTOLOGIA_GERAL, "CRO/PB"),
            new ProfessionData(ProfessionalType.DENTISTA, Specialty.ORTODONTIA, "CRO/PB"),
            new ProfessionData(ProfessionalType.DENTISTA, Specialty.IMPLANTODONTIA, "CRO/PB"));

    @Override
    public void run(String... args) {

        flyway.clean();
        flyway.migrate();

        try {

            seedPlans();

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
                    ProfessionalType.MEDICO,
                    Specialty.CARDIOLOGIA,
                    "CRM-TESTE-001");
            professionalService.approveApplication(professionalProfileId);

            String professionalUserId = professionalProfileRepository
                    .findById(professionalProfileId)
                    .map(p -> p.getUser().getId())
                    .orElse(null);

            String adminUserId = createAdmin(
                    "admin@example.com",
                    "admin1234",
                    "Admin Teste",
                    "00000000003");

            seedServicesAndProcedureRequests(professionalUserId, patientUserId);
            seedSchedule(professionalUserId, ScheduleTemplate.FULL_WEEK);

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

            String firstClinicId = createClinics(professionalProfileId, professionalUserId,
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
            createBulkAppointmentsForTestProfessional(professionalProfileId, patientUserIds);

            seedMedicalRecords(patientUserIds);
            seedSubscriptions(professionalUserId, professionalProfileIds);
            seedClinicalNotesAndExamRequests();
            seedNotifications(patientUserIds, professionalProfileIds);
            seedClinicWorkingHours();

            // Tables previously empty
            seedAddresses(patientUserId, professionalUserId, adminUserId, patientUserIds);
            seedEmergencyContacts(patientUserId, patientUserIds);
            seedAnamneses();
            seedSellers(adminUserId, professionalUserId, professionalProfileIds);
            seedCoupons(adminUserId);
            seedSubscriptionPayments();
            seedExamLabs();
            seedBillingPayments(patientUserId, professionalUserId, patientUserIds);

        } catch (Exception e) {
            log.error("Erro durante o seed:", e);
        }
    }

    // ─── Admin ──────────────────────────────────────────────────────────────────

    private String createAdmin(String email, String password, String name, String cpf) {
        try {
            if (userRepository.existsByEmail(email)) {
                return userRepository.findByEmail(email).map(User::getId).orElse(null);
            }
            User admin = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .cpf(cpf)
                    .phone("11900000003")
                    .birthDate(LocalDate.of(1980, 3, 10))
                    .gender(Gender.MALE)
                    .imageUrl("https://i.pravatar.cc/150?img=10")
                    .role(UserRole.ADMIN)
                    .build();
            User saved = userRepository.save(admin);
            log.info("Admin criado: {}", email);
            return saved.getId();
        } catch (Exception e) {
            log.warn("Erro ao criar admin: {}", e.getMessage());
            return null;
        }
    }

    // ─── Plans ──────────────────────────────────────────────────────────────────

    private void seedPlans() {
        record PlanDef(String slug, String name, String tier, BillingPeriod period,
                BigDecimal price, String features, int order) {
        }

        List<PlanDef> defs = List.of(
                new PlanDef("plan_basic", "Básico", "basic", BillingPeriod.MONTHLY,
                        new BigDecimal("49.90"),
                        "Até 50 consultas/mês;Agenda online;Suporte por email",
                        1),
                new PlanDef("plan_pro", "Pro", "pro", BillingPeriod.MONTHLY,
                        new BigDecimal("129.90"),
                        "Consultas ilimitadas;Teleconsulta;Prontuário digital;Suporte prioritário",
                        2),
                new PlanDef("plan_premium", "Premium", "premium", BillingPeriod.MONTHLY,
                        new BigDecimal("249.90"),
                        "Tudo do Pro;IA assistente;Gestão financeira;API de integração;Suporte 24/7",
                        3),
                new PlanDef("plan_pro_annual", "Pro Anual", "pro", BillingPeriod.ANNUAL,
                        new BigDecimal("1199.90"),
                        "Tudo do Pro;2 meses grátis;Suporte dedicado",
                        4));

        for (PlanDef def : defs) {
            try {
                if (planRepository.findBySlug(def.slug()).isEmpty()) {
                    planRepository.save(Plan.builder()
                            .slug(def.slug())
                            .name(def.name())
                            .tier(def.tier())
                            .billingPeriod(def.period())
                            .price(def.price())
                            .features(def.features())
                            .status(PlanStatus.ACTIVE)
                            .displayOrder(def.order())
                            .build());
                }
            } catch (Exception e) {
                log.warn("Erro ao criar plano {}: {}", def.slug(), e.getMessage());
            }
        }
        log.info("[Seed] Planos criados: {}", defs.size());
    }

    // ─── Addresses ──────────────────────────────────────────────────────────────

    private void seedAddresses(String patientUserId, String professionalUserId,
            String adminUserId, List<String> randomPatientIds) {

        record AddressDef(String userId, String zip, String street, String number,
                String district, String city, String state) {
        }

        List<AddressDef> fixed = new ArrayList<>();
        if (patientUserId != null)
            fixed.add(new AddressDef(patientUserId, "58000-000", "Rua das Flores", "123",
                    "Centro", "João Pessoa", "PB"));
        if (professionalUserId != null)
            fixed.add(new AddressDef(professionalUserId, "01310-100", "Av. Paulista", "1578",
                    "Bela Vista", "São Paulo", "SP"));
        if (adminUserId != null)
            fixed.add(new AddressDef(adminUserId, "70040-010", "SCS Quadra 2", "Bloco C",
                    "Asa Sul", "Brasília", "DF"));

        for (AddressDef def : fixed) {
            saveAddressIfMissing(def.userId(), def.zip(), def.street(), def.number(),
                    def.district(), def.city(), def.state());
        }

        List<String> cities = List.of("São Paulo", "Rio de Janeiro", "Belo Horizonte",
                "Curitiba", "Porto Alegre", "João Pessoa", "Campina Grande");
        List<String> states = List.of("SP", "RJ", "MG", "PR", "RS", "PB", "PB");

        int created = 0;
        for (int i = 0; i < Math.min(randomPatientIds.size(), 15); i++) {
            int idx = i % cities.size();
            created += saveAddressIfMissing(randomPatientIds.get(i),
                    "58" + String.format("%06d", faker.random().nextInt(999999)),
                    faker.address().streetName(),
                    String.valueOf(faker.random().nextInt(1, 999)),
                    faker.address().streetAddressNumber(),
                    cities.get(idx),
                    states.get(idx)) ? 1 : 0;
        }
        log.info("[Seed] Endereços criados: {}", fixed.size() + created);
    }

    private boolean saveAddressIfMissing(String userId, String zip, String street,
            String number, String district, String city, String state) {
        try {
            if (addressRepository.findByUserId(userId).isPresent()) return false;
            userRepository.findById(userId).ifPresent(user ->
                    addressRepository.save(Address.builder()
                            .user(user)
                            .zipCode(zip)
                            .street(street)
                            .number(number)
                            .district(district)
                            .city(city)
                            .state(state)
                            .build()));
            return true;
        } catch (Exception e) {
            log.debug("Erro ao criar endereço para userId={}: {}", userId, e.getMessage());
            return false;
        }
    }

    // ─── Emergency contacts ──────────────────────────────────────────────────────

    private void seedEmergencyContacts(String patientUserId, List<String> randomPatientIds) {
        List<String> allPatientIds = new ArrayList<>();
        if (patientUserId != null) allPatientIds.add(patientUserId);
        allPatientIds.addAll(randomPatientIds);

        int created = 0;
        for (String userId : allPatientIds) {
            try {
                var profile = patientProfileRepository.findByUserId(userId).orElse(null);
                if (profile == null) continue;
                if (!emergencyContactRepository.findByPatientProfileId(profile.getId()).isEmpty()) continue;
                emergencyContactRepository.save(EmergencyContact.builder()
                        .patientProfile(profile)
                        .name(faker.name().fullName())
                        .phone(faker.phoneNumber().cellPhone())
                        .build());
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar contato de emergência: {}", e.getMessage());
            }
        }
        log.info("[Seed] Contatos de emergência criados: {}", created);
    }

    // ─── Anamneses ──────────────────────────────────────────────────────────────

    private void seedAnamneses() {
        List<String> complaints = List.of(
                "Dor de cabeça frequente há 2 semanas",
                "Cansaço excessivo e falta de ar ao esforço",
                "Dor abdominal pós-refeição",
                "Pressão alta detectada em medição domiciliar",
                "Ansiedade e dificuldade para dormir",
                "Dor nas costas há 3 meses",
                "Tosse persistente há 10 dias");
        List<String> meds = List.of("Losartana 50mg", "Metformina 850mg", "Nenhum",
                "Omeprazol 20mg", "Dipirona 500mg se necessário");
        List<String> allergies = List.of("Nenhuma conhecida", "Penicilina", "Dipirona",
                "Látex", "Frutos do mar");
        List<String> histories = List.of(
                "Hipertensão diagnosticada em 2018",
                "Diabetes tipo 2 desde 2020",
                "Sem histórico relevante",
                "Pneumonia em 2019, apendicectomia em 2015");

        int created = 0;
        var completedAppointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .toList();

        for (var appointment : completedAppointments) {
            try {
                if (medicalHistoryRepository.findByAppointmentId(appointment.getId()).isPresent()) continue;
                if (faker.random().nextInt(100) < 60) {
                    medicalHistoryRepository.save(MedicalHistory.builder()
                            .appointment(appointment)
                            .chiefComplaint(complaints.get(faker.random().nextInt(complaints.size())))
                            .currentMedications(meds.get(faker.random().nextInt(meds.size())))
                            .allergies(allergies.get(faker.random().nextInt(allergies.size())))
                            .medicalHistory(histories.get(faker.random().nextInt(histories.size())))
                            .familyHistory(faker.bool().bool()
                                    ? "Hipertensão e diabetes na família"
                                    : "Sem histórico familiar relevante")
                            .observations(faker.lorem().sentence())
                            .build());
                    created++;
                }
            } catch (Exception e) {
                log.debug("Erro ao criar anamnese: {}", e.getMessage());
            }
        }
        log.info("[Seed] Anamneses criadas: {}", created);
    }

    // ─── Sellers ────────────────────────────────────────────────────────────────

    private void seedSellers(String adminUserId, String professionalUserId,
            List<String> professionalProfileIds) {

        if (professionalUserId != null && !sellerRepository.existsByUserId(professionalUserId)) {
            try {
                userRepository.findById(professionalUserId).ifPresent(user ->
                        sellerRepository.save(Seller.builder()
                                .user(user)
                                .slug("prof-teste")
                                .commissionRate(new BigDecimal("15.00"))
                                .status(SellerStatus.ACTIVE)
                                .pixKey("professional@example.com")
                                .notes("Vendedor de teste criado pelo seed")
                                .build()));
                log.info("[Seed] Seller de teste criado para professional@example.com");
            } catch (Exception e) {
                log.warn("Erro ao criar seller de teste: {}", e.getMessage());
            }
        }

        int created = 0;
        for (int i = 0; i < Math.min(professionalProfileIds.size(), 8); i++) {
            if (i % 3 != 0) continue;
            final int idx = i;
            try {
                String userId = professionalProfileRepository.findById(professionalProfileIds.get(idx))
                        .map(p -> p.getUser().getId())
                        .orElse(null);
                if (userId == null || sellerRepository.existsByUserId(userId)) continue;

                String slug = "afiliado-" + (i + 1);
                if (sellerRepository.existsBySlug(slug)) continue;

                userRepository.findById(userId).ifPresent(user -> {
                    SellerStatus status = faker.random().nextInt(100) < 80
                            ? SellerStatus.ACTIVE : SellerStatus.INACTIVE;
                    sellerRepository.save(Seller.builder()
                            .user(user)
                            .slug(slug)
                            .commissionRate(BigDecimal.valueOf(10 + faker.random().nextInt(11)))
                            .status(status)
                            .pixKey(user.getEmail())
                            .build());
                });
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar seller random: {}", e.getMessage());
            }
        }
        log.info("[Seed] Sellers adicionais criados: {}", created);

        seedSellerSales();
    }

    private void seedSellerSales() {
        List<Seller> sellers = sellerRepository.findAll();
        if (sellers.isEmpty()) return;

        List<Subscription> subscriptions = subscriptionRepository.findAll();
        if (subscriptions.isEmpty()) return;

        int created = 0;
        for (Subscription sub : subscriptions) {
            if (faker.random().nextInt(100) < 50) continue;
            try {
                if (sellerSaleRepository.findBySubscriptionId(sub.getId()).isPresent()) continue;
                Seller seller = sellers.get(faker.random().nextInt(sellers.size()));
                BigDecimal gross = BigDecimal.valueOf(50 + faker.random().nextInt(200));
                BigDecimal commission = gross.multiply(seller.getCommissionRate())
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                SellerSaleStatus status = faker.random().nextInt(100) < 60
                        ? SellerSaleStatus.PAID : SellerSaleStatus.PENDING;
                sellerSaleRepository.save(SellerSale.builder()
                        .seller(seller)
                        .subscription(sub)
                        .grossAmount(gross)
                        .commissionAmount(commission)
                        .monthReference(LocalDate.now().minusMonths(faker.random().nextInt(3)))
                        .status(status)
                        .paidAt(status == SellerSaleStatus.PAID
                                ? LocalDateTime.now().minusDays(faker.random().nextInt(30)) : null)
                        .build());
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar seller sale: {}", e.getMessage());
            }
        }
        log.info("[Seed] SellerSales criadas: {}", created);
    }

    // ─── Coupons ────────────────────────────────────────────────────────────────

    private void seedCoupons(String adminUserId) {
        record CouponDef(String code, String desc, CouponType type, BigDecimal value,
                Integer maxUses, String applicablePlans) {
        }

        List<CouponDef> defs = List.of(
                new CouponDef("BEMVINDO10", "10% de desconto para novos usuários",
                        CouponType.PERCENT, new BigDecimal("10.00"), 200, null),
                new CouponDef("PRO50OFF", "R$50 de desconto no plano Pro",
                        CouponType.FIXED, new BigDecimal("50.00"), 100, "plan_pro,plan_pro_annual"),
                new CouponDef("PREMIUM20", "20% de desconto no plano Premium",
                        CouponType.PERCENT, new BigDecimal("20.00"), 50, "plan_premium"),
                new CouponDef("TESTCOUPON", "Cupom de teste ilimitado",
                        CouponType.PERCENT, new BigDecimal("15.00"), null, null));

        List<Coupon> savedCoupons = new ArrayList<>();
        for (CouponDef def : defs) {
            try {
                if (couponRepository.findByCodeIgnoreCase(def.code()).isPresent()) continue;
                Coupon coupon = couponRepository.save(Coupon.builder()
                        .code(def.code())
                        .description(def.desc())
                        .type(def.type())
                        .value(def.value())
                        .maxUses(def.maxUses())
                        .applicablePlanIds(def.applicablePlans())
                        .status(CouponStatus.ACTIVE)
                        .createdBy(adminUserId)
                        .startsAt(LocalDateTime.now().minusDays(30))
                        .expiresAt(LocalDateTime.now().plusMonths(6))
                        .build());
                savedCoupons.add(coupon);
            } catch (Exception e) {
                log.warn("Erro ao criar cupom {}: {}", def.code(), e.getMessage());
            }
        }
        log.info("[Seed] Cupons criados: {}", savedCoupons.size());

        seedCouponUses(savedCoupons);
    }

    private void seedCouponUses(List<Coupon> coupons) {
        if (coupons.isEmpty()) return;
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        if (subscriptions.isEmpty()) return;

        int created = 0;
        for (int i = 0; i < Math.min(subscriptions.size(), 10); i++) {
            if (faker.random().nextInt(100) < 40) continue;
            try {
                Subscription sub = subscriptions.get(i);
                Coupon coupon = coupons.get(faker.random().nextInt(coupons.size()));
                BigDecimal discount = coupon.getType() == CouponType.PERCENT
                        ? new BigDecimal("99.90").multiply(coupon.getValue())
                                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
                        : coupon.getValue();
                couponUseRepository.save(CouponUse.builder()
                        .coupon(coupon)
                        .userId(sub.getUser().getId())
                        .subscriptionId(sub.getId())
                        .discountApplied(discount)
                        .build());
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar coupon use: {}", e.getMessage());
            }
        }
        log.info("[Seed] CouponUses criados: {}", created);
    }

    // ─── Subscription payments ───────────────────────────────────────────────────

    private void seedSubscriptionPayments() {
        List<Subscription> active = subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .toList();

        List<String> methods = List.of("credit_card", "pix", "boleto");
        int created = 0;

        for (Subscription sub : active) {
            int payments = faker.random().nextInt(1, 4);
            for (int i = 0; i < payments; i++) {
                try {
                    BigDecimal gross = BigDecimal.valueOf(49.90 + faker.random().nextInt(200));
                    BigDecimal fee = gross.multiply(new BigDecimal("0.039"))
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                    BigDecimal net = gross.subtract(fee);
                    subscriptionPaymentRepository.save(SubscriptionPayment.builder()
                            .subscriptionId(sub.getId())
                            .mpPaymentId("mp_" + faker.random().nextInt(100000, 999999))
                            .grossAmount(gross)
                            .processingFee(fee)
                            .netAmount(net)
                            .paymentMethod(methods.get(faker.random().nextInt(methods.size())))
                            .paidAt(LocalDateTime.now().minusDays(i * 30L + faker.random().nextInt(5)))
                            .build());
                    created++;
                } catch (Exception e) {
                    log.debug("Erro ao criar subscription payment: {}", e.getMessage());
                }
            }
        }
        log.info("[Seed] SubscriptionPayments criados: {}", created);
    }

    // ─── Original methods ────────────────────────────────────────────────────────

    private record CityLocation(String city, String state, double lat, double lng) {
    }

    private String createClinics(String testProfessionalProfileId, String professionalUserId,
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
                        professionalUserId),
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
                professionalProfileRepository.findById(def.ownerProfileId()).ifPresent(p -> {
                    p.setCity(def.location().city());
                    p.setState(def.location().state());
                    p.setLatitude(def.location().lat() + (faker.random().nextDouble() * 0.02 - 0.01));
                    p.setLongitude(def.location().lng() + (faker.random().nextDouble() * 0.02 - 0.01));
                    professionalProfileRepository.save(p);
                });

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

                if (firstClinicId == null) firstClinicId = clinic.getId();

                int added = 0;
                for (String extraId : extraProfessionalProfileIds) {
                    if (added >= 2) break;
                    if (extraId.equals(def.ownerProfileId())) continue;
                    try {
                        clinicService.addMember(clinic.getId(), extraId, ownerUserId);
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
            String email, String password, String name, String cpf,
            String clinicId, String ownerUserId) {
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
            String email, String password, String name, String cpf, String imageUrl) {
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
        return userService.createUser(dto).getId();
    }

    private String createProfessional(
            String email, String password, String name, String cpf,
            ProfessionalType profession, Specialty specialty, String licenseNumber) {
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

    private void createTestAppointments(String patientUserId, String testProfessionalProfileId) {
        PatientProfile profile = patientProfileRepository.findByUserId(patientUserId).orElse(null);
        if (profile == null) return;

        String[] reasons = { "Consulta de rotina", "Dor no peito", "Check-up anual", "Pressão alta", "Retorno" };
        int[] daysAhead = { -10, -5, 1, 7, 20 };
        int[] hours = { 9, 11, 14, 10, 15 };
        AppointmentStatus[] statuses = {
                AppointmentStatus.COMPLETED, AppointmentStatus.COMPLETED,
                AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING, AppointmentStatus.PENDING
        };

        for (int i = 0; i < reasons.length; i++) {
            try {
                var response = appointmentService.scheduleAppointment(
                        patientUserId,
                        CreateAppointmentDTO.builder()
                                .professionalId(testProfessionalProfileId)
                                .scheduledAt(LocalDateTime.now().plusDays(daysAhead[i])
                                        .withHour(hours[i]).withMinute(0).withSecond(0).withNano(0))
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

        try {
            var onlineConfirmed = appointmentService.scheduleAppointment(
                    patientUserId,
                    CreateAppointmentDTO.builder()
                            .professionalId(testProfessionalProfileId)
                            .scheduledAt(LocalDateTime.now().plusDays(3)
                                    .withHour(16).withMinute(0).withSecond(0).withNano(0))
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
                            .scheduledAt(LocalDateTime.now().plusDays(12)
                                    .withHour(9).withMinute(30).withSecond(0).withNano(0))
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
            String testProfessionalProfileId, List<String> patientUserIds) {

        List<AppointmentStatus> statuses = List.of(
                AppointmentStatus.COMPLETED, AppointmentStatus.CONFIRMED,
                AppointmentStatus.PENDING, AppointmentStatus.CANCELED);
        List<String> reasons = List.of("Consulta de rotina", "Check-up anual", "Dor de cabeça",
                "Pressão alta", "Retorno médico", "Exames laboratoriais",
                "Avaliação clínica", "Consulta preventiva");

        int created = 0;
        for (int i = 0; i < 100; i++) {
            try {
                String patientId = patientUserIds.get(faker.random().nextInt(patientUserIds.size()));
                AppointmentStatus status = statuses.get(faker.random().nextInt(statuses.size()));
                LocalDateTime scheduledAt = resolveScheduledAt(status);
                LocalDateTime safeDate = LocalDateTime.now().plusDays(1)
                        .withHour(10).withMinute(0).withSecond(0).withNano(0);
                var response = appointmentService.scheduleAppointment(
                        patientId,
                        CreateAppointmentDTO.builder()
                                .professionalId(testProfessionalProfileId)
                                .scheduledAt(safeDate)
                                .reason(reasons.get(faker.random().nextInt(reasons.size())))
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
                log.debug("Erro ao criar consulta fake para profissional teste: {}", e.getMessage());
            }
        }
        log.info("Criadas {} consultas para o profissional de teste", created);
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
                        .birthDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        .gender(faker.bool().bool() ? Gender.MALE : Gender.FEMALE)
                        .imageUrl("https://i.pravatar.cc/150?img=" + faker.random().nextInt(1, 70))
                        .build();
                var userResponse = userService.createUser(patientDTO);
                patientProfileRepository.findByUserId(userResponse.getId()).ifPresent(profile -> {
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
        if (professionalUserId == null) return;

        try {
            setConsultationPriceService.execute(professionalUserId, new BigDecimal("250.00"));
        } catch (Exception e) {
            log.warn("Erro ao definir preço de consulta no seed: {}", e.getMessage());
        }

        record ServiceDef(String name, String description, BigDecimal price, int duration,
                boolean requiresConsultation) {
        }

        List<ServiceDef> services = List.of(
                new ServiceDef("Consulta de Cardiologia", "Consulta clínica de cardiologia",
                        new BigDecimal("250.00"), 30, false),
                new ServiceDef("ECG - Eletrocardiograma", "Exame do ritmo cardíaco em repouso",
                        new BigDecimal("180.00"), 20, false),
                new ServiceDef("Holter 24h", "Monitoramento cardíaco contínuo de 24 horas",
                        new BigDecimal("350.00"), 60, true),
                new ServiceDef("Ecocardiograma", "Ultrassom do coração com avaliação funcional",
                        new BigDecimal("450.00"), 60, true),
                new ServiceDef("MAPA", "Monitoramento ambulatorial da pressão arterial",
                        new BigDecimal("320.00"), 45, true));

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
                if (def.requiresConsultation()) requiresConsultationServiceIds.add(created.getId());
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
        if (userId == null) return;
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
            if (userId == null) continue;
            if (i % 5 == 0 || i % 5 == 1 || i % 5 == 2) {
                seedSchedule(userId, templates[i % templates.length]);
                assigned++;
            }
        }
        log.info("[Seed] Schedules seeded for {}/{} random professionals", assigned, professionalProfileIds.size());
    }

    private String generateFakeCPF() {
        StringBuilder cpf = new StringBuilder();
        for (int i = 0; i < 11; i++) cpf.append(faker.random().nextInt(0, 9));
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

    private void enrichCompletedAppointment(com.consultafacil.domain.entity.Appointment appointment) {
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
        List<ExamType> exams = List.of(ExamType.HEMOGRAMA_COMPLETO, ExamType.GLICEMIA_JEJUM,
                ExamType.ELETROCARDIOGRAMA, ExamType.RAIO_X, ExamType.ULTRASSOM_ABDOMINAL,
                ExamType.TSH, ExamType.COLESTEROL_TOTAL);
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
        record NotifTemplate(NotificationType type, String title, String message) {
        }

        List<NotifTemplate> templates = List.of(
                new NotifTemplate(NotificationType.APPOINTMENT_SCHEDULED,
                        "Consulta agendada", "Sua consulta foi agendada com sucesso."),
                new NotifTemplate(NotificationType.APPOINTMENT_CONFIRMED,
                        "Consulta confirmada", "Sua consulta foi confirmada pelo profissional."),
                new NotifTemplate(NotificationType.APPOINTMENT_CANCELED,
                        "Consulta cancelada", "Sua consulta foi cancelada."),
                new NotifTemplate(NotificationType.GENERAL,
                        "Resultado disponível", "O resultado do seu exame está disponível."));

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

    // ─── Exam Labs ───────────────────────────────────────────────────────────────

    private void seedExamLabs() {
        record LabDef(String name, String description, String phone, String address,
                String city, String state, double lat, double lng, String imageUrl,
                List<ExamType> acceptedExams) {
        }

        List<LabDef> defs = List.of(
                new LabDef(
                        "Laboratório Saúde Total",
                        "Exames laboratoriais completos com resultados online em 24h",
                        "(83) 3224-1100",
                        "Av. Epitácio Pessoa, 2490 — Bessa",
                        "João Pessoa", "PB", -7.1105, -34.8239,
                        "https://images.unsplash.com/photo-1579165466741-7f35e4755182?w=600",
                        List.of(ExamType.HEMOGRAMA_COMPLETO, ExamType.GLICEMIA_JEJUM, ExamType.TSH,
                                ExamType.COLESTEROL_TOTAL, ExamType.CREATININA, ExamType.TGO,
                                ExamType.TGP, ExamType.PSA, ExamType.VITAMINA_D)),
                new LabDef(
                        "Clínica Diagnóstica Paraibana",
                        "Imagem e análises clínicas de alta precisão",
                        "(83) 3311-4422",
                        "Rua Cardoso Vieira, 180 — Miramar",
                        "João Pessoa", "PB", -7.1158, -34.8611,
                        "https://images.unsplash.com/photo-1581595220892-b0739db3ba8c?w=600",
                        List.of(ExamType.RAIO_X, ExamType.ULTRASSOM_ABDOMINAL, ExamType.ULTRASSOM_PELVICO,
                                ExamType.ECOCARDIOGRAMA, ExamType.HOLTER_24H, ExamType.MAPA,
                                ExamType.TOMOGRAFIA)),
                new LabDef(
                        "LabClin Campina Grande",
                        "Referência em análises clínicas no Agreste paraibano",
                        "(83) 3322-5500",
                        "Av. Assis Chateaubriand, 1200",
                        "Campina Grande", "PB", -7.2258, -35.8811,
                        "https://images.unsplash.com/photo-1530026405186-ed1f139313f8?w=600",
                        List.of(ExamType.HEMOGRAMA_COMPLETO, ExamType.GLICEMIA_JEJUM,
                                ExamType.HEMOGLOBINA_GLICADA, ExamType.COLESTEROL_TOTAL,
                                ExamType.UROCULTURA, ExamType.URINA_TIPO_I, ExamType.SOROLOGIAS_HIV)),
                new LabDef(
                        "Laboratório Einstein SP",
                        "Exames de alta complexidade com tecnologia de ponta",
                        "(11) 3747-1000",
                        "Av. Albert Einstein, 627 — Morumbi",
                        "São Paulo", "SP", -23.5978, -46.7195,
                        "https://images.unsplash.com/photo-1576765608866-5b51046452be?w=600",
                        List.of(ExamType.HEMOGRAMA_COMPLETO, ExamType.GLICEMIA_JEJUM, ExamType.TSH,
                                ExamType.COLESTEROL_TOTAL, ExamType.RAIO_X,
                                ExamType.RESSONANCIA_MAGNETICA, ExamType.TOMOGRAFIA, ExamType.PET_CT)),
                new LabDef(
                        "Centro de Imagem Rio",
                        "Diagnóstico por imagem no Rio de Janeiro",
                        "(21) 2253-7700",
                        "Rua Visconde de Pirajá, 550 — Ipanema",
                        "Rio de Janeiro", "RJ", -22.9840, -43.2053,
                        "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=600",
                        List.of(ExamType.ULTRASSONOGRAFIA, ExamType.RAIO_X, ExamType.ECOCARDIOGRAMA,
                                ExamType.HOLTER_24H, ExamType.MAPA,
                                ExamType.MAMOGRAFIA, ExamType.DENSITOMETRIA_OSSEA)));

        record DaySlot(String day, LocalTime open, LocalTime close, int duration, boolean isOpen) {
        }

        List<DaySlot> weekdays = List.of(
                new DaySlot("MONDAY", LocalTime.of(7, 0), LocalTime.of(18, 0), 30, true),
                new DaySlot("TUESDAY", LocalTime.of(7, 0), LocalTime.of(18, 0), 30, true),
                new DaySlot("WEDNESDAY", LocalTime.of(7, 0), LocalTime.of(18, 0), 30, true),
                new DaySlot("THURSDAY", LocalTime.of(7, 0), LocalTime.of(18, 0), 30, true),
                new DaySlot("FRIDAY", LocalTime.of(7, 0), LocalTime.of(17, 0), 30, true),
                new DaySlot("SATURDAY", LocalTime.of(7, 0), LocalTime.of(12, 0), 30, true),
                new DaySlot("SUNDAY", LocalTime.of(7, 0), LocalTime.of(12, 0), 30, false));

        int created = 0;
        for (LabDef def : defs) {
            try {
                if (!examLabRepository.findByStatus("ACTIVE").stream()
                        .anyMatch(l -> l.getName().equals(def.name()))) {
                    ExamLab lab = examLabRepository.save(ExamLab.builder()
                            .name(def.name())
                            .description(def.description())
                            .phone(def.phone())
                            .address(def.address())
                            .city(def.city())
                            .state(def.state())
                            .latitude(def.lat())
                            .longitude(def.lng())
                            .imageUrl(def.imageUrl())
                            .acceptedExams(new ArrayList<>(def.acceptedExams()))
                            .status("ACTIVE")
                            .build());

                    for (DaySlot slot : weekdays) {
                        examLabHoursRepository.save(ExamLabHours.builder()
                                .examLab(lab)
                                .dayOfWeek(slot.day())
                                .openTime(slot.open())
                                .closeTime(slot.close())
                                .slotDurationMinutes(slot.duration())
                                .isOpen(slot.isOpen())
                                .build());
                    }
                    created++;
                }
            } catch (Exception e) {
                log.warn("Erro ao criar exam lab {}: {}", def.name(), e.getMessage());
            }
        }
        log.info("[Seed] ExamLabs criados: {}", created);
    }

    // ─── Billing payments ────────────────────────────────────────────────────────

    private void seedBillingPayments(String patientUserId, String professionalUserId,
            List<String> randomPatientIds) {

        if (billingPaymentRepository.count() > 0) return;

        record PaymentDef(String payerId, String payerName, String payerEmail,
                PaymentType type, OwnerType ownerType, String ownerId,
                BigDecimal amount, BigDecimal systemFee, String method,
                BillingPaymentStatus status, int daysAgo) {
        }

        List<String> methods = List.of("credit_card", "pix", "credit_card", "debit_card");

        record PaymentTypeConfig(PaymentType type, BigDecimal fixedFee, double pctFee) {
        }
        List<PaymentTypeConfig> typeConfigs = List.of(
                new PaymentTypeConfig(PaymentType.CONSULTATION, new BigDecimal("5.00"), 0.00),
                new PaymentTypeConfig(PaymentType.PROCEDURE,    new BigDecimal("10.00"), 0.02),
                new PaymentTypeConfig(PaymentType.EXAM,         new BigDecimal("3.00"),  0.00),
                new PaymentTypeConfig(PaymentType.SUBSCRIPTION, BigDecimal.ZERO, 0.00));

        List<PaymentDef> defs = new ArrayList<>();

        // Fixed test payments for test users
        defs.add(new PaymentDef(patientUserId, "Paciente Teste", "patient@example.com",
                PaymentType.CONSULTATION, OwnerType.DOCTOR, professionalUserId,
                new BigDecimal("250.00"), new BigDecimal("5.00"), "credit_card",
                BillingPaymentStatus.PAID, 30));
        defs.add(new PaymentDef(patientUserId, "Paciente Teste", "patient@example.com",
                PaymentType.EXAM, OwnerType.DOCTOR, professionalUserId,
                new BigDecimal("180.00"), new BigDecimal("3.00"), "pix",
                BillingPaymentStatus.PAID, 15));
        defs.add(new PaymentDef(patientUserId, "Paciente Teste", "patient@example.com",
                PaymentType.PROCEDURE, OwnerType.DOCTOR, professionalUserId,
                new BigDecimal("350.00"), new BigDecimal("17.00"), "credit_card",
                BillingPaymentStatus.PAID, 7));
        defs.add(new PaymentDef(patientUserId, "Paciente Teste", "patient@example.com",
                PaymentType.CONSULTATION, OwnerType.DOCTOR, professionalUserId,
                new BigDecimal("250.00"), new BigDecimal("5.00"), "pix",
                BillingPaymentStatus.PENDING, 1));
        defs.add(new PaymentDef(patientUserId, "Paciente Teste", "patient@example.com",
                PaymentType.SUBSCRIPTION, null, null,
                new BigDecimal("129.90"), BigDecimal.ZERO, "credit_card",
                BillingPaymentStatus.PAID, 45));

        // Random payments for other patients
        List<String> samplePatients = randomPatientIds.stream().limit(10).toList();
        List<BillingPaymentStatus> statusPool = List.of(
                BillingPaymentStatus.PAID, BillingPaymentStatus.PAID, BillingPaymentStatus.PAID,
                BillingPaymentStatus.PENDING, BillingPaymentStatus.FAILED, BillingPaymentStatus.CANCELED);

        for (int i = 0; i < samplePatients.size(); i++) {
            String userId = samplePatients.get(i);
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;
            int numPayments = 2 + faker.random().nextInt(4);
            for (int j = 0; j < numPayments; j++) {
                PaymentTypeConfig cfg = typeConfigs.get(faker.random().nextInt(typeConfigs.size() - 1));
                BigDecimal amount = BigDecimal.valueOf(100 + faker.random().nextInt(400));
                BigDecimal fee = cfg.fixedFee().add(
                        amount.multiply(BigDecimal.valueOf(cfg.pctFee()))
                                .setScale(2, java.math.RoundingMode.HALF_UP));
                BillingPaymentStatus status = statusPool.get(faker.random().nextInt(statusPool.size()));
                defs.add(new PaymentDef(userId, user.getName(), user.getEmail(),
                        cfg.type(), OwnerType.DOCTOR, professionalUserId, amount, fee,
                        methods.get(faker.random().nextInt(methods.size())), status,
                        faker.random().nextInt(1, 120)));
            }
        }

        int paymentCount = 0;
        int invoiceCount = 0;
        int seq = 1;

        for (PaymentDef def : defs) {
            try {
                BigDecimal net = def.amount().subtract(def.systemFee());
                LocalDateTime createdAt = LocalDateTime.now().minusDays(def.daysAgo());
                LocalDateTime paidAt = def.status() == BillingPaymentStatus.PAID ? createdAt.plusMinutes(5) : null;

                BillingPayment payment = billingPaymentRepository.save(BillingPayment.builder()
                        .paymentType(def.type())
                        .ownerType(def.ownerType())
                        .ownerId(def.ownerId())
                        .amount(def.amount())
                        .systemFee(def.systemFee())
                        .gatewayFee(BigDecimal.ZERO)
                        .netAmount(net)
                        .currency("BRL")
                        .paymentMethod(def.method())
                        .gateway("MOCK")
                        .gatewayPaymentId("MOCK-SEED-" + String.format("%06d", seq))
                        .status(def.status())
                        .payerId(def.payerId())
                        .payerName(def.payerName())
                        .payerEmail(def.payerEmail())
                        .description(def.type().name().charAt(0)
                                + def.type().name().substring(1).toLowerCase()
                                + " — seed data")
                        .paidAt(paidAt)
                        .createdAt(createdAt)
                        .build());
                paymentCount++;

                if (def.status() == BillingPaymentStatus.PAID) {
                    invoiceRepository.save(Invoice.builder()
                            .payment(payment)
                            .invoiceNumber("INV-" + String.format("%06d", seq))
                            .createdAt(createdAt.plusMinutes(5))
                            .build());
                    invoiceCount++;
                }
                seq++;
            } catch (Exception e) {
                log.debug("Erro ao criar billing payment seed: {}", e.getMessage());
            }
        }
        log.info("[Seed] BillingPayments criados: {}, Invoices: {}", paymentCount, invoiceCount);
    }

    private void seedClinicWorkingHours() {
        record DaySlot(String day, LocalTime open, LocalTime close, boolean isOpen) {
        }
        List<DaySlot> slots = List.of(
                new DaySlot("MONDAY", LocalTime.of(8, 0), LocalTime.of(18, 0), true),
                new DaySlot("TUESDAY", LocalTime.of(8, 0), LocalTime.of(18, 0), true),
                new DaySlot("WEDNESDAY", LocalTime.of(8, 0), LocalTime.of(18, 0), true),
                new DaySlot("THURSDAY", LocalTime.of(8, 0), LocalTime.of(18, 0), true),
                new DaySlot("FRIDAY", LocalTime.of(8, 0), LocalTime.of(17, 0), true),
                new DaySlot("SATURDAY", LocalTime.of(8, 0), LocalTime.of(12, 0), false),
                new DaySlot("SUNDAY", LocalTime.of(8, 0), LocalTime.of(12, 0), false));

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
