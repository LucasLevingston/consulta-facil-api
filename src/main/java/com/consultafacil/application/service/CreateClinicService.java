package com.consultafacil.application.service;

import com.consultafacil.api.dto.clinic.ClinicResponseDTO;
import com.consultafacil.api.dto.clinic.CreateClinicDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.ClinicMember;
import com.consultafacil.domain.entity.ClinicMemberId;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.ClinicRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.consultafacil.application.port.in.CreateClinicUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateClinicService implements CreateClinicUseCase {

    private final ClinicRepositoryPort clinicRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final UserRepositoryPort userRepository;
    private final ClinicMapper clinicMapper;

    @Transactional
    public ClinicResponseDTO execute(String userId, CreateClinicDTO dto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Clinic clinic = Clinic.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .imageUrl(dto.getImageUrl())
                .owner(owner)
                .build();

        Clinic saved = clinicRepository.save(clinic);

        professionalProfileRepository.findByUserId(userId).ifPresent(professionalProfile -> {
            ClinicMember member = ClinicMember.builder()
                    .id(new ClinicMemberId(saved.getId(), professionalProfile.getId()))
                    .clinic(saved)
                    .professionalProfile(professionalProfile)
                    .role("OWNER")
                    .build();
            saved.getMembers().add(member);
        });

        clinicRepository.saveAndFlush(saved);
        return clinicMapper.toResponseDTO(saved);
    }
}
