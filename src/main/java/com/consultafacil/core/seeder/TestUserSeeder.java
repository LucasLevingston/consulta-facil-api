package com.consultafacil.core.seeder;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.application.port.in.ApproveApplicationUseCase;
import com.consultafacil.application.port.in.CreateProfessionalProfileUseCase;
import com.consultafacil.application.port.in.RegisterUserUseCase;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.repository.ProfessionalProfileRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TestUserSeeder {

    private final RegisterUserUseCase registerUser;
    private final CreateProfessionalProfileUseCase createProfessionalProfile;
    private final ApproveApplicationUseCase approveApplication;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public String createPatient(String email, String password, String name, String cpf, String imageUrl) {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password).cpf(cpf)
                .phone("11900000001").birthDate(LocalDate.of(1990, 1, 15))
                .gender(Gender.MALE).imageUrl(imageUrl).build();
        return registerUser.execute(dto).getId();
    }

    public String createProfessional(String email, String password, String name, String cpf,
            ProfessionalType profession, Specialty specialty, String licenseNumber) {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password).cpf(cpf)
                .phone("11900000002").birthDate(LocalDate.of(1985, 6, 20))
                .imageUrl("https://i.pravatar.cc/150?img=" + faker.random().nextInt(1, 70))
                .gender(Gender.MALE).build();
        var userResponse = registerUser.execute(dto);
        var profResponse = createProfessionalProfile.execute(userResponse.getId(),
                CreateProfessionalDTO.builder().profession(profession).specialty(specialty)
                        .licenseNumber(licenseNumber).build());
        approveApplication.execute(profResponse.getId());
        return profResponse.getId();
    }

    public String getUserIdForProfile(String professionalProfileId) {
        return professionalProfileRepository.findById(professionalProfileId)
                .map(p -> p.getUser().getId()).orElse(null);
    }
}
