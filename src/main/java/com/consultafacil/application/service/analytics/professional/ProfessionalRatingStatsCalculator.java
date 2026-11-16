package com.consultafacil.application.service.analytics.professional;

import com.consultafacil.api.dto.analytics.professional.RatingStatsDTO;
import com.consultafacil.domain.entity.Appointment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ProfessionalRatingStatsCalculator {

    public RatingStatsDTO calculate(List<Appointment> appointments) {
        List<Integer> ratings = appointments.stream()
                .map(Appointment::getRating)
                .filter(Objects::nonNull)
                .filter(r -> r >= 1 && r <= 5)
                .toList();

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int stars = 1; stars <= 5; stars++) distribution.put(stars, 0L);
        ratings.forEach(r -> distribution.merge(r, 1L, Long::sum));

        Double average = ratings.isEmpty() ? null
                : BigDecimal.valueOf(ratings.stream().mapToInt(Integer::intValue).average().orElse(0))
                        .setScale(1, RoundingMode.HALF_UP).doubleValue();
        return new RatingStatsDTO(average, ratings.size(), distribution);
    }
}
