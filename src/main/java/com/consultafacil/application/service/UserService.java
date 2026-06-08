package com.consultafacil.application.service;

import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.core.util.PiiMask;
import com.consultafacil.api.dto.user.UserResponseDTO;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.consultafacil.application.port.in.RegisterUserUseCase;
import com.consultafacil.application.port.in.UserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase, RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final StoragePort storagePort;

    @Override
    @Transactional
    public UserResponseDTO createUser(CreateUserDTO dto) {
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

        return toResponseDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(String id) {
        log.debug("Fetching user by ID: {}", id);
        return toResponseDTO(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", PiiMask.maskEmail(email));
        return toResponseDTO(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found")));
    }

    @Override
    @Transactional
    public UserResponseDTO uploadAvatar(String userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getImageId() != null) {
            storagePort.delete(user.getImageId());
        }
        try {
            String imageUrl = storagePort.upload(file.getBytes(), file.getOriginalFilename(), file.getContentType(), "avatars");
            String imageId = imageUrl.substring(imageUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
            user.updateAvatar(imageUrl, imageId);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to upload avatar", e);
        }
        return toResponseDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userRepository.delete(user);
    }

    @Override
    public UserResponseDTO getById(String id) { return getUserById(id); }

    @Override
    public UserResponseDTO getByEmail(String email) { return getUserByEmail(email); }

    @Override
    public UserResponseDTO execute(CreateUserDTO dto) { return createUser(dto); }

    private UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .cpf(user.getCpf())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .imageUrl(user.getImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
