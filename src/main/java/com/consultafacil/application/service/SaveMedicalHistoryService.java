package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.MedicalHistoryResponseDTO;
import com.consultafacil.api.dto.appointment.SaveMedicalHistoryDTO;
import com.consultafacil.application.port.in.SaveMedicalHistoryUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.MedicalHistory;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.MedicalHistoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaveMedicalHistoryService implements SaveMedicalHistoryUseCase {

    private final MedicalHistoryRepositoryPort medicalHistoryRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final OwnershipValidator ownershipValidator;
    private final MedicalHistoryMapper mapper;

    @Override
    @Transactional
    public MedicalHistoryResponseDTO execute(String appointmentId, String userId, SaveMedicalHistoryDTO dto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        ownershipValidator.verifyAppointmentAccess(appointment, userId);

        MedicalHistory medicalHistory = medicalHistoryRepository.findByAppointmentId(appointmentId)
                .orElse(MedicalHistory.builder().appointment(appointment).build());

        medicalHistory.setChiefComplaint(dto.getChiefComplaint());
        medicalHistory.setCurrentMedications(dto.getCurrentMedications());
        medicalHistory.setAllergies(dto.getAllergies());
        medicalHistory.setMedicalHistory(dto.getMedicalHistory());
        medicalHistory.setFamilyHistory(dto.getFamilyHistory());
        medicalHistory.setObservations(dto.getObservations());

        return mapper.toResponseDTO(medicalHistoryRepository.save(medicalHistory));
    }
}
