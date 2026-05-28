package com.example.consulta.application.service;

import com.example.consulta.api.dto.appointment.ClinicalNoteResponseDTO;
import com.example.consulta.api.dto.appointment.SaveClinicalNoteDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.core.security.OwnershipValidator;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.entity.ClinicalNote;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.ClinicalNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClinicalNoteService {

    private final ClinicalNoteRepository clinicalNoteRepository;
    private final AppointmentRepository appointmentRepository;
    private final OwnershipValidator ownershipValidator;

    @Transactional(readOnly = true)
    public Optional<ClinicalNoteResponseDTO> getByAppointmentId(String appointmentId, String authenticatedUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        ownershipValidator.verifyAppointmentAccess(appointment, authenticatedUserId);
        return clinicalNoteRepository.findByAppointmentId(appointmentId)
                .map(this::toResponseDTO);
    }

    @Transactional
    public ClinicalNoteResponseDTO save(String appointmentId, String userId, SaveClinicalNoteDTO dto) {
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

        return toResponseDTO(clinicalNoteRepository.save(clinicalNote));
    }

    private ClinicalNoteResponseDTO toResponseDTO(ClinicalNote p) {
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
