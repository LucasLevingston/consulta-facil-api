package com.example.consulta.application.service;

import com.example.consulta.api.dto.appointment.AnamneseResponseDTO;
import com.example.consulta.api.dto.appointment.SaveAnamneseDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.core.security.OwnershipValidator;
import com.example.consulta.domain.entity.Anamnese;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.repository.AnamneseRepository;
import com.example.consulta.domain.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnamneseService {

    private final AnamneseRepository anamneseRepository;
    private final AppointmentRepository appointmentRepository;
    private final OwnershipValidator ownershipValidator;

    @Transactional(readOnly = true)
    public Optional<AnamneseResponseDTO> getByAppointmentId(String appointmentId, String authenticatedUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        ownershipValidator.verifyAppointmentAccess(appointment, authenticatedUserId);
        return anamneseRepository.findByAppointmentId(appointmentId)
                .map(this::toResponseDTO);
    }

    @Transactional
    public AnamneseResponseDTO save(String appointmentId, String userId, SaveAnamneseDTO dto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada: " + appointmentId));

        ownershipValidator.verifyAppointmentAccess(appointment, userId);

        Anamnese anamnese = anamneseRepository.findByAppointmentId(appointmentId)
                .orElse(Anamnese.builder().appointment(appointment).build());

        anamnese.setChiefComplaint(dto.getChiefComplaint());
        anamnese.setCurrentMedications(dto.getCurrentMedications());
        anamnese.setAllergies(dto.getAllergies());
        anamnese.setMedicalHistory(dto.getMedicalHistory());
        anamnese.setFamilyHistory(dto.getFamilyHistory());
        anamnese.setObservations(dto.getObservations());

        return toResponseDTO(anamneseRepository.save(anamnese));
    }

    private AnamneseResponseDTO toResponseDTO(Anamnese a) {
        return AnamneseResponseDTO.builder()
                .id(a.getId())
                .appointmentId(a.getAppointment().getId())
                .chiefComplaint(a.getChiefComplaint())
                .currentMedications(a.getCurrentMedications())
                .allergies(a.getAllergies())
                .medicalHistory(a.getMedicalHistory())
                .familyHistory(a.getFamilyHistory())
                .observations(a.getObservations())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
