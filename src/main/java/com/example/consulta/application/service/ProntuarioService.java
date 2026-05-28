package com.example.consulta.application.service;

import com.example.consulta.api.dto.appointment.ProntuarioResponseDTO;
import com.example.consulta.api.dto.appointment.SaveProntuarioDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.core.security.OwnershipValidator;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.entity.Prontuario;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.ProntuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;
    private final AppointmentRepository appointmentRepository;
    private final OwnershipValidator ownershipValidator;

    @Transactional(readOnly = true)
    public Optional<ProntuarioResponseDTO> getByAppointmentId(String appointmentId, String authenticatedUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        ownershipValidator.verifyAppointmentAccess(appointment, authenticatedUserId);
        return prontuarioRepository.findByAppointmentId(appointmentId)
                .map(this::toResponseDTO);
    }

    @Transactional
    public ProntuarioResponseDTO save(String appointmentId, String userId, SaveProntuarioDTO dto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada: " + appointmentId));

        ownershipValidator.verifyProfessionalOwnership(appointment, userId);

        Prontuario prontuario = prontuarioRepository.findByAppointmentId(appointmentId)
                .orElse(Prontuario.builder().appointment(appointment).build());

        prontuario.setClinicalNotes(dto.getClinicalNotes());
        prontuario.setDiagnosis(dto.getDiagnosis());
        prontuario.setDiagnosisCid(dto.getDiagnosisCid());
        prontuario.setPrescription(dto.getPrescription());
        prontuario.setExamRequests(dto.getExamRequests());
        prontuario.setTreatmentPlan(dto.getTreatmentPlan());
        prontuario.setFollowUpInstructions(dto.getFollowUpInstructions());

        return toResponseDTO(prontuarioRepository.save(prontuario));
    }

    private ProntuarioResponseDTO toResponseDTO(Prontuario p) {
        return ProntuarioResponseDTO.builder()
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
