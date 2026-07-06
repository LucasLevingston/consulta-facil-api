package com.consultafacil.core.seeder;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.application.service.ProfessionalService;
import com.consultafacil.application.service.UserService;
import com.consultafacil.domain.enums.Gender;
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
public class BulkProfessionalSeeder {

    private final UserService userService;
    private final ProfessionalService professionalService;
    private final ProfessionDataProvider professionDataProvider;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public List<String> seed(int count) {
        List<String> ids = new ArrayList<>();
        var professionData = professionDataProvider.getAll();
        int created = 0;
        for (int i = 0; created < count; i++) {
            var pd = professionData.get(i % professionData.size());
            try {
                CreateUserDTO userDTO = CreateUserDTO.builder()
                        .name(faker.name().fullName())
                        .email(faker.internet().emailAddress())
                        .password("prof1234")
                        .cpf(CpfGeneratorUtils.generateFakeCPF(faker))
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
                var profResponse = professionalService.createProfile(userResponse.getId(), profDTO);
                ids.add(profResponse.getId());
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar profissional fake: {}", e.getMessage());
                created++;
            }
        }
        return ids;
    }

    public void approveAll(List<String> professionalProfileIds) {
        professionalProfileIds.forEach(id -> {
            try {
                professionalService.approveApplication(id);
            } catch (Exception e) {
                log.debug("Erro ao aprovar profissional {}: {}", id, e.getMessage());
            }
        });
    }
}
