package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.appointment.CallNextPatientUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CallNextPatientService implements CallNextPatientUseCase {

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

        appointment.callNext(); // validates CHECKED_IN status, sets IN_PROGRESS + calledAt
        Appointment saved = appointmentRepository.save(appointment);

        return toResponseDTO(saved);
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
                .checkedInAt(a.getCheckedInAt())
                .calledAt(a.getCalledAt())
                .reason(a.getReason())
                .notes(a.getNotes())
                .modality(a.getModality())
                .meetLink(a.getMeetLink())
                .status(a.getStatus())
                .paymentStatus(a.getPaymentStatus())
                .paymentAmount(a.getPaymentAmount())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
