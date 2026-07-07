package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.GenerateMeetLinkUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenerateMeetLinkService implements GenerateMeetLinkUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

    @Transactional
    public AppointmentResponseDTO execute(String appointmentId, String professionalUserId) {
        var profile = professionalProfileRepository.findByUserId(professionalUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", professionalUserId));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (!appointment.getProfessional().getId().equals(profile.getId())) {
            throw new BadRequestException("Appointment does not belong to this professional");
        }

        if (appointment.getModality() != AppointmentModality.ONLINE) {
            throw new BadRequestException("Cannot generate meet link for in-person appointment");
        }

        appointment.setMeetLink(generateMeetUrl());
        Appointment saved = appointmentRepository.save(appointment);
        return toResponseDTO(saved);
    }

    private String generateMeetUrl() {
        String uid = UUID.randomUUID().toString().replace("-", "");
        return "https://meet.google.com/" + uid.substring(0, 3) + "-" + uid.substring(3, 7) + "-" + uid.substring(7, 10);
    }

    private AppointmentResponseDTO toResponseDTO(Appointment a) {
        return AppointmentResponseDTO.builder()
                .id(a.getId())
                .patientName(a.getPatient().getUser().getName())
                .patientId(a.getPatient().getId())
                .professionalName(a.getProfessional().getUser().getName())
                .professionalId(a.getProfessional().getId())
                .specialty(a.getProfessional().getSpecialty() != null ? a.getProfessional().getSpecialty().name() : null)
                .scheduledAt(a.getScheduledAt())
                .previousScheduledAt(a.getPreviousScheduledAt())
                .checkedInAt(a.getCheckedInAt())
                .calledAt(a.getCalledAt())
                .reason(a.getReason())
                .notes(a.getNotes())
                .modality(a.getModality())
                .meetLink(a.getMeetLink())
                .status(a.getStatus())
                .cancellationReason(a.getCancellationReason())
                .paymentStatus(a.getPaymentStatus())
                .paymentAmount(a.getPaymentAmount())
                .rating(a.getRating())
                .ratingComment(a.getRatingComment())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
