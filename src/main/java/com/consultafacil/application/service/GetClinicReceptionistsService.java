package com.consultafacil.application.service;

import com.consultafacil.api.dto.receptionist.ReceptionistResponseDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.port.out.ClinicReceptionistRepositoryPort;
import com.consultafacil.domain.port.out.ClinicRepositoryPort;
import com.consultafacil.application.port.in.GetClinicReceptionistsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetClinicReceptionistsService implements GetClinicReceptionistsUseCase {

    private final ClinicRepositoryPort clinicRepository;
    private final ClinicReceptionistRepositoryPort clinicReceptionistRepository;

    @Transactional(readOnly = true)
    public List<ReceptionistResponseDTO> execute(String clinicId, String requestingUserId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(requestingUserId)) {
            throw new BadRequestException("Only clinic owner can view receptionists");
        }

        return clinicReceptionistRepository.findByClinicId(clinicId).stream()
                .map(r -> ReceptionistResponseDTO.builder()
                        .id(r.getId())
                        .userId(r.getUser().getId())
                        .name(r.getUser().getName())
                        .email(r.getUser().getEmail())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();
    }
}
