package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.ClinicalNoteResponseDTO;
import com.consultafacil.api.dto.appointment.SaveClinicalNoteDTO;
import com.consultafacil.application.port.in.SaveClinicalNoteUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.ClinicalNote;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.ClinicalNoteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaveClinicalNoteService implements SaveClinicalNoteUseCase {

    private final ClinicalNoteRepositoryPort clinicalNoteRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final OwnershipValidator ownershipValidator;

    @Override
    @Transactional
    public ClinicalNoteResponseDTO execute(String appointmentId, String userId, SaveClinicalNoteDTO dto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        ownershipValidator.verifyProfessionalOwnership(appointment, userId);

        ClinicalNote clinicalNote = clinicalNoteRepository.findByAppointmentId(appointmentId)
                .orElse(ClinicalNote.builder().appointment(appointment).build());

        clinicalNote.setClinicalNotes(dto.getClinicalNotes());
        clinicalNote.setDiagnosis(dto.getDiagnosis());
        clinicalNote.setDiagnosisCid(dto.getDiagnosisCid());
        clinicalNote.setPrescription(dto.getPrescription());
        clinicalNote.setExamRequests(dto.getExamRequests());
        clinicalNote.setTreatmentPlan(dto.getTreatmentPlan());
        clinicalNote.setFollowUpInstructions(dto.getFollowUpInstructions());

        return GetClinicalNoteService.toResponseDTO(clinicalNoteRepository.save(clinicalNote));
    }
}
