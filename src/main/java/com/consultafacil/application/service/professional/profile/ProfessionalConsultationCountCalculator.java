package com.consultafacil.application.service.professional.profile;

import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalConsultationCountCalculator {

    public int computeConsultationCount(ProfessionalProfile profile) {
        return (int) profile.getAppointments().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();
    }
}
