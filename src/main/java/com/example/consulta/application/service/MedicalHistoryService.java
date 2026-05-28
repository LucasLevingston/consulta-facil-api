package com.example.consulta.application.service;

import com.example.consulta.api.dto.appointment.MedicalHistoryResponseDTO;
import com.example.consulta.api.dto.appointment.SaveMedicalHistoryDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.MedicalHistory;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.repository.MedicalHistoryRepository;
import com.example.consulta.domain.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicalHistoryService {

    private final MedicalHistoryRepository medicalHistoryRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public Optional<MedicalHistoryResponseDTO> getByAppointmentId(String appointmentId) {
        return medicalHistoryRepository.findByAppointmentId(appointmentId)
                .map(this::toResponseDTO);
    }

    @Transactional
    public MedicalHistoryResponseDTO save(String appointmentId, String userId, SaveMedicalHistoryDTO dto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada: " + appointmentId));

        String patientUserId = appointment.getPatient().getUser().getId();
        String professionalUserId = appointment.getProfessional().getUser().getId();
        if (!userId.equals(patientUserId) && !userId.equals(professionalUserId)) {
            throw new BadRequestException("Você não tem permissão para preencher a medicalHistory desta consulta");
        }

        MedicalHistory medicalHistory = medicalHistoryRepository.findByAppointmentId(appointmentId)
                .orElse(MedicalHistory.builder().appointment(appointment).build());

        medicalHistory.setChiefComplaint(dto.getChiefComplaint());
        medicalHistory.setCurrentMedications(dto.getCurrentMedications());
        medicalHistory.setAllergies(dto.getAllergies());
        medicalHistory.setMedicalHistory(dto.getMedicalHistory());
        medicalHistory.setFamilyHistory(dto.getFamilyHistory());
        medicalHistory.setObservations(dto.getObservations());

        return toResponseDTO(medicalHistoryRepository.save(medicalHistory));
    }

    private MedicalHistoryResponseDTO toResponseDTO(MedicalHistory a) {
        return MedicalHistoryResponseDTO.builder()
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
