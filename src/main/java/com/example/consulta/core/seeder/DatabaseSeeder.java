package com.example.consulta.core.seeder;

import com.example.consulta.application.service.AppointmentService;
import com.example.consulta.application.service.DoctorService;
import com.example.consulta.application.service.UserService;
import com.example.consulta.api.dto.appointment.CreateAppointmentDTO;
import com.example.consulta.api.dto.doctor.CreateDoctorDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.PatientProfileRepository;
import com.example.consulta.domain.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    private final Faker faker = new Faker(new Locale("pt-BR"));
    private final List<String> specialties = List.of(
            "Cardiologia", "Dermatologia", "Oftalmologia", "Pediatria",
            "Clinica Geral", "Neurologia", "Ortopedia", "Gastroenterologia",
            "Pneumologia", "Psiquiatria");

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Iniciando seed do banco de dados...");

        try {
            String testPatientUserId = createPatientIfAbsent("patient@example.com", "123456", "Paciente Teste",
                    "00000000001");
            String testDoctorProfileId = createDoctorIfAbsent("doctor@example.com", "123456", "Dr. Doutor Teste",
                    "00000000002", "Cardiologia", "CRM-TESTE-001");
            createDoctorIfAbsent("admin@example.com", "123456", "Admin Teste", "00000000003", "Clinica Geral",
                    "CRM-ADMIN-001");

            if (userRepository.count() <= 3) {
                List<String> patientUserIds = createPatients(5);
                List<String> doctorProfileIds = createDoctors(10);
                createAppointments(patientUserIds, doctorProfileIds);
                log.info("Seed com dados fake concluído!");
            } else {
                log.info("Dados fake já existem, pulando.");
            }

            if (testPatientUserId != null && testDoctorProfileId != null) {
                createTestAppointments(testPatientUserId, testDoctorProfileId);
            }
        } catch (Exception e) {
            log.error("Erro durante o seed: ", e);
        }
    }

    private String createPatientIfAbsent(String email, String password, String name, String cpf) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseGet(() -> {
                    CreateUserDTO dto = CreateUserDTO.builder()
                            .name(name).email(email).password(password).cpf(cpf)
                            .phone("11900000001").birthDate(LocalDate.of(1990, 1, 15))
                            .gender(Gender.MALE).build();
                    var created = userService.createUser(dto);
                    setAvatar(created.getId(), "https://i.pravatar.cc/150?img=3");
                    log.info("Usuário de teste criado: {}", email);
                    return created.getId();
                });
    }

    private String createDoctorIfAbsent(String email, String password, String name, String cpf,
            String specialty, String licenseNumber) {
        var existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return doctorService.getDoctorByUserId(existingUser.get().getId()).getId();
        }

        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password).cpf(cpf)
                .phone("11900000002").birthDate(LocalDate.of(1985, 6, 20))
                .gender(Gender.MALE).build();
        var userResponse = userService.createUser(dto);
        setAvatar(userResponse.getId(), "https://i.pravatar.cc/150?img=12");

        var doctorResponse = doctorService.createDoctorProfile(userResponse.getId(),
                CreateDoctorDTO.builder().specialty(specialty).licenseNumber(licenseNumber).build());
        log.info("Médico de teste criado: {}", email);
        return doctorResponse.getId();
    }

    private void forceStatus(String appointmentId, AppointmentStatus status) {
        appointmentRepository.findById(appointmentId).ifPresent(a -> {
            a.setStatus(status);
            appointmentRepository.save(a);
        });
    }

    private void setAvatar(String userId, String avatarUrl) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setImageUrl(avatarUrl);
            userRepository.save(user);
        });
    }

    private void createTestAppointments(String patientUserId, String testDoctorProfileId) {
        PatientProfile profile = patientProfileRepository.findByUserId(patientUserId).orElse(null);
        if (profile == null)
            return;

        if (!appointmentRepository.findByPatientId(profile.getId(), Pageable.ofSize(1)).isEmpty()) {
            log.info("Consultas de teste já existem, pulando.");
            return;
        }

        String[] reasons = { "Consulta de rotina", "Dor no peito", "Check-up anual", "Pressão alta", "Retorno" };
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
                var response = appointmentService.scheduleAppointment(patientUserId, CreateAppointmentDTO.builder()
                        .doctorId(testDoctorProfileId)
                        .scheduledAt(LocalDateTime.now().plusDays(daysAhead[i]).withHour(hours[i]).withMinute(0)
                                .withSecond(0).withNano(0))
                        .reason(reasons[i])
                        .notes(faker.lorem().sentence())
                        .build());
                forceStatus(response.getId(), statuses[i]);
            } catch (Exception e) {
                log.debug("Erro ao criar consulta de teste: {}", e.getMessage());
            }
        }
        log.info("Consultas de teste criadas para o paciente.");
    }

    private List<String> createPatients(int count) {
        log.info("Criando {} pacientes fake...", count);
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
                        .build();

                var userResponse = userService.createUser(patientDTO);
                setAvatar(userResponse.getId(), "https://i.pravatar.cc/150?img=" + (i + 20));

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

    private List<String> createDoctors(int count) {
        log.info("Criando {} médicos fake...", count);
        List<String> doctorProfileIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            try {
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
                setAvatar(userResponse.getId(), "https://i.pravatar.cc/150?img=" + (i + 40));

                CreateDoctorDTO doctorDTO = CreateDoctorDTO.builder()
                        .specialty(specialties.get(i % specialties.size()))
                        .licenseNumber("CRM" + System.currentTimeMillis() + i)
                        .build();

                var doctorResponse = doctorService.createDoctorProfile(userResponse.getId(), doctorDTO);
                doctorProfileIds.add(doctorResponse.getId());
            } catch (Exception e) {
                log.debug("Erro ao criar médico fake: {}", e.getMessage());
            }
        }

        return doctorProfileIds;
    }

    private void createAppointments(List<String> patientUserIds, List<String> doctorProfileIds) {
        log.info("Criando consultas fake...");
        int count = 0;

        for (String userId : patientUserIds) {
            int total = faker.random().nextInt(1, 4);
            for (int i = 0; i < total; i++) {
                String doctorId = doctorProfileIds.get(faker.random().nextInt(doctorProfileIds.size()));
                LocalDateTime time = LocalDateTime.now()
                        .plusDays(faker.random().nextInt(1, 60))
                        .withHour(faker.random().nextInt(8, 18))
                        .withMinute(0).withSecond(0).withNano(0);

                try {
                    var resp = appointmentService.scheduleAppointment(userId, CreateAppointmentDTO.builder()
                            .doctorId(doctorId)
                            .scheduledAt(time)
                            .reason(faker.medical().medicineName())
                            .notes(faker.lorem().sentence())
                            .build());
                    int roll = faker.random().nextInt(3);
                    if (roll == 1) forceStatus(resp.getId(), AppointmentStatus.CONFIRMED);
                    count++;
                } catch (Exception e) {
                    log.debug("Erro ao criar consulta fake: {}", e.getMessage());
                }
            }
        }

        log.info("Criadas {} consultas fake", count);
    }

    private String generateFakeCPF() {
        StringBuilder cpf = new StringBuilder();
        for (int i = 0; i < 11; i++)
            cpf.append(faker.random().nextInt(0, 9));
        return cpf.toString();
    }
}
