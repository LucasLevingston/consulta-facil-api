package com.example.consulta.application.service;

import com.example.consulta.api.dto.receptionist.InviteReceptionistDTO;
import com.example.consulta.api.dto.receptionist.ReceptionistResponseDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Clinic;
import com.example.consulta.domain.entity.ClinicReceptionist;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.repository.ClinicReceptionistRepository;
import com.example.consulta.domain.repository.ClinicRepository;
import com.example.consulta.domain.repository.UserRepository;
import com.example.consulta.application.port.in.InviteReceptionistUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteReceptionistService implements InviteReceptionistUseCase {

    private final ClinicRepository clinicRepository;
    private final UserRepository userRepository;
    private final ClinicReceptionistRepository clinicReceptionistRepository;

    @Transactional
    public ReceptionistResponseDTO execute(String clinicId, String ownerUserId, InviteReceptionistDTO dto) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(ownerUserId)) {
            throw new BadRequestException("Only clinic owner can invite receptionists");
        }

        User receptionist = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + dto.getEmail(), dto.getEmail()));

        if (clinicReceptionistRepository.existsByClinicIdAndUserId(clinicId, receptionist.getId())) {
            throw new BadRequestException("User is already a receptionist of this clinic");
        }

        receptionist.setRole(UserRole.RECEPTIONIST);
        userRepository.save(receptionist);

        ClinicReceptionist entry = ClinicReceptionist.builder()
                .id(UUID.randomUUID().toString())
                .clinic(clinic)
                .user(receptionist)
                .build();

        ClinicReceptionist saved = clinicReceptionistRepository.save(entry);

        return toDTO(saved);
    }

    private ReceptionistResponseDTO toDTO(ClinicReceptionist r) {
        return ReceptionistResponseDTO.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .name(r.getUser().getName())
                .email(r.getUser().getEmail())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
