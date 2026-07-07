package com.consultafacil.core.seeder;

import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.application.port.in.RegisterUserUseCase;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.repository.PatientProfileRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkPatientSeeder {

    private final RegisterUserUseCase registerUser;
    private final PatientProfileRepository patientProfileRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public List<String> seed(int count) {
        List<String> userIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                CreateUserDTO patientDTO = CreateUserDTO.builder()
                        .name(faker.name().fullName())
                        .email(faker.internet().emailAddress())
                        .password("patient123")
                        .cpf(CpfGeneratorUtils.generateFakeCPF(faker))
                        .phone(faker.phoneNumber().cellPhone())
                        .birthDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        .gender(faker.bool().bool() ? Gender.MALE : Gender.FEMALE)
                        .imageUrl("https://i.pravatar.cc/150?img=" + faker.random().nextInt(1, 70))
                        .build();
                var userResponse = registerUser.execute(patientDTO);
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
}
