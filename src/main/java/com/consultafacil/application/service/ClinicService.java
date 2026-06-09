package com.consultafacil.application.service;

import com.consultafacil.api.dto.clinic.ClinicMemberDTO;
import com.consultafacil.api.dto.clinic.ClinicResponseDTO;
import com.consultafacil.api.dto.clinic.CreateClinicDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.ClinicMember;
import com.consultafacil.domain.entity.ClinicMemberId;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.ClinicMemberRepositoryPort;
import com.consultafacil.domain.port.out.ClinicRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.consultafacil.application.port.in.ClinicUseCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicService implements ClinicUseCase {

    private final ClinicRepositoryPort clinicRepository;
    private final ClinicMemberRepositoryPort clinicMemberRepository;

    @PersistenceContext
    private EntityManager em;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final UserRepositoryPort userRepository;

    @Transactional
    public ClinicResponseDTO createClinic(String userId, CreateClinicDTO dto) {
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
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> getAllClinics() {
        return clinicRepository.findByStatus("ACTIVE").stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ClinicResponseDTO getClinicById(String clinicId) {
        return toDTO(clinicRepository.findByIdWithMembers(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId)));
    }

    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> getMyClinic(String userId) {
        return clinicRepository.findByOwnerId(userId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public ClinicResponseDTO updateClinic(String clinicId, String userId, CreateClinicDTO dto) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(userId)) {
            throw new BadRequestException("You are not the owner of this clinic");
        }

        clinic.setName(dto.getName());
        clinic.setDescription(dto.getDescription());
        clinic.setPhone(dto.getPhone());
        clinic.setAddress(dto.getAddress());
        clinic.setCity(dto.getCity());
        clinic.setState(dto.getState());
        clinic.setZipCode(dto.getZipCode());
        clinic.setLatitude(dto.getLatitude());
        clinic.setLongitude(dto.getLongitude());
        if (dto.getImageUrl() != null) clinic.setImageUrl(dto.getImageUrl());

        return toDTO(clinicRepository.save(clinic));
    }

    @Transactional
    public void addMember(String clinicId, String professionalProfileId, String requesterId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(requesterId)) {
            throw new BadRequestException("Only the clinic owner can add members");
        }

        ProfessionalProfile professional = professionalProfileRepository.findById(professionalProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalProfileId));

        if (clinicMemberRepository.existsByClinicIdAndProfessionalProfileId(clinicId, professionalProfileId)) {
            throw new BadRequestException("Professional is already a member of this clinic");
        }

        ClinicMember member = ClinicMember.builder()
                .id(new ClinicMemberId(clinicId, professionalProfileId))
                .clinic(clinic)
                .professionalProfile(professional)
                .role("MEMBER")
                .build();
        clinicMemberRepository.save(member);
        em.flush();
        em.clear();
    }

    @Transactional
    public void removeMember(String clinicId, String professionalProfileId, String requesterId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(requesterId)) {
            throw new BadRequestException("Only the clinic owner can remove members");
        }

        ClinicMemberId id = new ClinicMemberId(clinicId, professionalProfileId);
        clinicMemberRepository.deleteById(id);
        em.flush();
        em.clear();
    }

    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> getClinicsNearby(double lat, double lng, double radiusKm) {
        return clinicRepository.findNearby(lat, lng, radiusKm).stream().map(this::toDTO).toList();
    }

    private ClinicResponseDTO toDTO(Clinic clinic) {
        List<ClinicMemberDTO> members = clinic.getMembers().stream()
                .map(m -> ClinicMemberDTO.builder()
                        .professionalProfileId(m.getProfessionalProfile().getId())
                        .professionalName(m.getProfessionalProfile().getUser().getName())
                        .specialty(m.getProfessionalProfile().getSpecialty())
                        .imageUrl(m.getProfessionalProfile().getUser().getImageUrl())
                        .role(m.getRole())
                        .build())
                .toList();

        return ClinicResponseDTO.builder()
                .id(clinic.getId())
                .name(clinic.getName())
                .description(clinic.getDescription())
                .phone(clinic.getPhone())
                .address(clinic.getAddress())
                .city(clinic.getCity())
                .state(clinic.getState())
                .zipCode(clinic.getZipCode())
                .latitude(clinic.getLatitude())
                .longitude(clinic.getLongitude())
                .imageUrl(clinic.getImageUrl())
                .status(clinic.getStatus())
                .ownerId(clinic.getOwner().getId())
                .ownerName(clinic.getOwner().getName())
                .members(members)
                .createdAt(clinic.getCreatedAt())
                .build();
    }
}
