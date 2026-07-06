package com.consultafacil.application.service;

import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalRatingCalculator {

    public Double computeRating(ProfessionalProfile profile) {
        return profile.getAppointments().stream()
                .filter(a -> a.getRating() != null)
                .mapToInt(a -> a.getRating())
                .average()
                .stream()
                .boxed()
                .map(avg -> Math.round(avg * 10.0) / 10.0)
                .findFirst()
                .orElse(null);
    }

    public int computeConsultationCount(ProfessionalProfile profile) {
        return (int) profile.getAppointments().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();
    }
}
