package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.MedicalHistoryResponseDTO;
import com.consultafacil.application.port.in.GetMedicalHistoryUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.MedicalHistoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetMedicalHistoryService implements GetMedicalHistoryUseCase {

    private final MedicalHistoryRepositoryPort medicalHistoryRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final OwnershipValidator ownershipValidator;
    private final MedicalHistoryMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<MedicalHistoryResponseDTO> execute(String appointmentId, String authenticatedUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        ownershipValidator.verifyAppointmentAccess(appointment, authenticatedUserId);
        return medicalHistoryRepository.findByAppointmentId(appointmentId)
                .map(mapper::toResponseDTO);
    }
}
