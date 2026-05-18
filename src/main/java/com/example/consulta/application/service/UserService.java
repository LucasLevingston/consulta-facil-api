package com.example.consulta.application.service;

import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.api.dto.user.UserResponseDTO;
import com.example.consulta.core.exception.DuplicateResourceException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.repository.PatientProfileRepository;
import com.example.consulta.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    @Transactional
    public UserResponseDTO createUser(CreateUserDTO dto) {
        log.info("Creating new user: {}", dto.getEmail());

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
                .role(UserRole.USER)
                .build();

        User savedUser = userRepository.save(user);

        PatientProfile patientProfile = PatientProfile.builder()
                .user(savedUser)
                .build();
        patientProfileRepository.save(patientProfile);

        log.info("User created successfully: {}", savedUser.getId());
        return toResponseDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(String id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));
        return toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO uploadAvatar(String userId, MultipartFile file) {
        log.info("Uploading avatar for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getImageId() != null) {
            s3Service.delete(user.getImageId());
        }

        String imageUrl = s3Service.upload(file, "avatars");
        String imageId = imageUrl.substring(imageUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());

        user.setImageUrl(imageUrl);
        user.setImageId(imageId);
        User savedUser = userRepository.save(user);

        log.info("Avatar uploaded for user: {}", userId);
        return toResponseDTO(savedUser);
    }

    @Transactional
    public void deleteUser(String id) {
        log.info("Deleting user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userRepository.delete(user);
        log.info("User deleted successfully: {}", id);
    }

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
