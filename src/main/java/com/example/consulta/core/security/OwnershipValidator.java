package com.example.consulta.core.security;

import com.example.consulta.domain.entity.Appointment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class OwnershipValidator {

    public void verifyAppointmentAccess(Appointment appointment, String userId) {
        if (!isParticipant(appointment, userId)) {
            throw new AccessDeniedException("Access denied to appointment " + appointment.getId());
        }
    }

    public void verifyPatientOwnership(Appointment appointment, String userId) {
        if (!appointment.getPatient().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied: only the patient of this appointment can perform this action");
        }
    }

    public void verifyProfessionalOwnership(Appointment appointment, String userId) {
        if (!appointment.getProfessional().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied: only the professional of this appointment can perform this action");
        }
    }

    private boolean isParticipant(Appointment appointment, String userId) {
        return appointment.getPatient().getUser().getId().equals(userId)
                || appointment.getProfessional().getUser().getId().equals(userId);
    }
}
