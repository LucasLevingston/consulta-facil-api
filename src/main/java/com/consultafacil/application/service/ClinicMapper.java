package com.consultafacil.application.service;

import com.consultafacil.api.dto.clinic.ClinicMemberDTO;
import com.consultafacil.api.dto.clinic.ClinicResponseDTO;
import com.consultafacil.domain.entity.Clinic;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClinicMapper {

    public ClinicResponseDTO toResponseDTO(Clinic clinic) {
        List<ClinicMemberDTO> members = clinic.getMembers().stream()
                .map(m -> ClinicMemberDTO.builder()
                        .professionalProfileId(m.getProfessionalProfile().getId())
                        .professionalName(m.getProfessionalProfile().getUser().getName())
                        .specialty(m.getProfessionalProfile().getSpecialty() != null ? m.getProfessionalProfile().getSpecialty().name() : null)
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
