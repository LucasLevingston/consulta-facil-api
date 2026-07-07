package com.consultafacil.application.service.professional.profile;

import com.consultafacil.domain.entity.ProfessionalProfile;
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
}
