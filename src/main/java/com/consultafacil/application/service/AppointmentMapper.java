package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.domain.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .patientName(appointment.getPatient() != null && appointment.getPatient().getUser() != null
                        ? appointment.getPatient().getUser().getName() : null)
                .patientId(appointment.getPatient() != null ? appointment.getPatient().getId() : null)
                .professionalName(appointment.getProfessional() != null && appointment.getProfessional().getUser() != null
                        ? appointment.getProfessional().getUser().getName() : null)
                .professionalId(appointment.getProfessional() != null ? appointment.getProfessional().getId() : null)
                .specialty(appointment.getProfessional() != null && appointment.getProfessional().getSpecialty() != null ? appointment.getProfessional().getSpecialty().name() : null)
                .scheduledAt(appointment.getScheduledAt())
                .previousScheduledAt(appointment.getPreviousScheduledAt())
                .checkedInAt(appointment.getCheckedInAt())
                .calledAt(appointment.getCalledAt())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .modality(appointment.getModality())
                .meetLink(appointment.getMeetLink())
                .status(appointment.getStatus())
                .cancellationReason(appointment.getCancellationReason())
                .paymentStatus(appointment.getPaymentStatus())
                .paymentAmount(appointment.getPaymentAmount())
                .chosenPaymentMethod(appointment.getChosenPaymentMethod())
                .paymentTiming(appointment.getProfessional() != null ? appointment.getProfessional().getPaymentTiming() : null)
                .rating(appointment.getRating())
                .ratingComment(appointment.getRatingComment())
                .serviceId(appointment.getService() != null ? appointment.getService().getId() : null)
                .serviceName(appointment.getService() != null ? appointment.getService().getName() : null)
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
