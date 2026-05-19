package com.example.consulta.application.service;

import com.example.consulta.api.dto.clinic.ClinicMemberDTO;
import com.example.consulta.api.dto.clinic.ClinicResponseDTO;
import com.example.consulta.api.dto.clinic.CreateClinicDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Clinic;
import com.example.consulta.domain.entity.ClinicMember;
import com.example.consulta.domain.entity.ClinicMemberId;
import com.example.consulta.domain.entity.DoctorProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.repository.ClinicMemberRepository;
import com.example.consulta.domain.repository.ClinicRepository;
import com.example.consulta.domain.repository.DoctorProfileRepository;
import com.example.consulta.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;
    private final ClinicMemberRepository clinicMemberRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;

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

        doctorProfileRepository.findByUserId(userId).ifPresent(doctorProfile -> {
            ClinicMember member = ClinicMember.builder()
                    .id(new ClinicMemberId(saved.getId(), doctorProfile.getId()))
                    .clinic(saved)
                    .doctorProfile(doctorProfile)
                    .role("OWNER")
                    .build();
            clinicMemberRepository.save(member);
        });

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> getAllClinics() {
        return clinicRepository.findByStatus("ACTIVE").stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ClinicResponseDTO getClinicById(String clinicId) {
        return toDTO(clinicRepository.findById(clinicId)
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
    public void addMember(String clinicId, String doctorProfileId, String requesterId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(requesterId)) {
            throw new BadRequestException("Apenas o proprietário pode adicionar membros");
        }

        DoctorProfile doctor = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorProfileId));

        if (clinicMemberRepository.existsByClinicIdAndDoctorProfileId(clinicId, doctorProfileId)) {
            throw new BadRequestException("Médico já é membro desta clínica");
        }

        ClinicMember member = ClinicMember.builder()
                .id(new ClinicMemberId(clinicId, doctorProfileId))
                .clinic(clinic)
                .doctorProfile(doctor)
                .role("MEMBER")
                .build();
        clinicMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(String clinicId, String doctorProfileId, String requesterId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(requesterId)) {
            throw new BadRequestException("Apenas o proprietário pode remover membros");
        }

        ClinicMemberId id = new ClinicMemberId(clinicId, doctorProfileId);
        clinicMemberRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> getClinicsNearby(double lat, double lng, double radiusKm) {
        return clinicRepository.findNearby(lat, lng, radiusKm).stream().map(this::toDTO).toList();
    }

    private ClinicResponseDTO toDTO(Clinic clinic) {
        List<ClinicMemberDTO> members = clinic.getMembers().stream()
                .map(m -> ClinicMemberDTO.builder()
                        .doctorProfileId(m.getDoctorProfile().getId())
                        .doctorName(m.getDoctorProfile().getUser().getName())
                        .specialty(m.getDoctorProfile().getSpecialty())
                        .imageUrl(m.getDoctorProfile().getUser().getImageUrl())
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
