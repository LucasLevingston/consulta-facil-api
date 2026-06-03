package com.example.consulta.application.service;

import com.example.consulta.api.dto.clinic.ClinicMemberDTO;
import com.example.consulta.api.dto.clinic.ClinicResponseDTO;
import com.example.consulta.api.dto.clinic.CreateClinicDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Clinic;
import com.example.consulta.domain.entity.ClinicMember;
import com.example.consulta.domain.entity.ClinicMemberId;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.port.out.ClinicMemberRepositoryPort;
import com.example.consulta.domain.port.out.ClinicRepositoryPort;
import com.example.consulta.domain.port.out.ProfessionalProfileRepositoryPort;
import com.example.consulta.domain.port.out.UserRepositoryPort;
import com.example.consulta.application.port.in.ClinicUseCase;
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
            throw new BadRequestException("Você não é o proprietário desta clínica");
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
            throw new BadRequestException("Apenas o proprietário pode adicionar membros");
        }

        ProfessionalProfile professional = professionalProfileRepository.findById(professionalProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalProfileId));

        if (clinicMemberRepository.existsByClinicIdAndProfessionalProfileId(clinicId, professionalProfileId)) {
            throw new BadRequestException("Profissional já é membro desta clínica");
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
            throw new BadRequestException("Apenas o proprietário pode remover membros");
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
