package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.ClinicalNoteResponseDTO;
import com.consultafacil.application.port.in.appointment.GetClinicalNoteUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.ClinicalNote;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.appointment.ClinicalNoteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetClinicalNoteService implements GetClinicalNoteUseCase {

    private final ClinicalNoteRepositoryPort clinicalNoteRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final OwnershipValidator ownershipValidator;

    @Override
    @Transactional(readOnly = true)
    public Optional<ClinicalNoteResponseDTO> execute(String appointmentId, String authenticatedUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        ownershipValidator.verifyAppointmentAccess(appointment, authenticatedUserId);
        return clinicalNoteRepository.findByAppointmentId(appointmentId)
                .map(GetClinicalNoteService::toResponseDTO);
    }

    static ClinicalNoteResponseDTO toResponseDTO(ClinicalNote p) {
        return ClinicalNoteResponseDTO.builder()
                .id(p.getId())
                .appointmentId(p.getAppointment().getId())
                .clinicalNotes(p.getClinicalNotes())
                .diagnosis(p.getDiagnosis())
                .diagnosisCid(p.getDiagnosisCid())
                .prescription(p.getPrescription())
                .examRequests(p.getExamRequests())
                .treatmentPlan(p.getTreatmentPlan())
                .followUpInstructions(p.getFollowUpInstructions())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
