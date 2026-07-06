package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String createAdmin(String email, String password, String name, String cpf) {
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
}
