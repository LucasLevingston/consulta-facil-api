package com.consultafacil.application.service;

import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.api.dto.user.UserResponseDTO;
import com.consultafacil.application.port.in.RegisterUserUseCase;
import com.consultafacil.application.port.in.ReferralUseCase;
import com.consultafacil.application.port.in.WalletUseCase;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletUseCase walletUseCase;
    private final ReferralUseCase referralUseCase;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserResponseDTO execute(CreateUserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("User", "email", dto.getEmail());
        }
        if (dto.getCpf() != null && userRepository.existsByCpf(dto.getCpf())) {
            throw new DuplicateResourceException("User", "CPF", dto.getCpf());
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .cpf(dto.getCpf())
                .phone(dto.getPhone())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .imageUrl(dto.getImageUrl())
                .role(UserRole.PATIENT)
                .build();

        User savedUser = userRepository.save(user);
        patientProfileRepository.save(PatientProfile.builder().user(savedUser).build());
        walletUseCase.createWallet(savedUser.getId());
        referralUseCase.getOrCreateReferralCode(savedUser.getId());

        return mapper.toResponseDTO(savedUser);
    }
}
