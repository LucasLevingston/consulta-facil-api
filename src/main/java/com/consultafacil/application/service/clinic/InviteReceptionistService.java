package com.consultafacil.application.service.clinic;

import com.consultafacil.api.dto.receptionist.InviteReceptionistDTO;
import com.consultafacil.api.dto.receptionist.ReceptionistResponseDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.ClinicReceptionist;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.ClinicReceptionistRepositoryPort;
import com.consultafacil.domain.port.out.ClinicRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.consultafacil.application.port.in.InviteReceptionistUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteReceptionistService implements InviteReceptionistUseCase {

    private final ClinicRepositoryPort clinicRepository;
    private final UserRepositoryPort userRepository;
    private final ClinicReceptionistRepositoryPort clinicReceptionistRepository;

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
