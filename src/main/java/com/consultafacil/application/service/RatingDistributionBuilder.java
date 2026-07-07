package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalRatingDTO;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class RatingDistributionBuilder {

    public ProfessionalRatingDTO build(Double average, List<Object[]> rows) {
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            distribution.put(i, 0L);
        }
        for (Object[] row : rows) {
            distribution.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        Double rounded = average != null ? Math.round(average * 10.0) / 10.0 : null;
        return new ProfessionalRatingDTO(rounded, total, distribution);
    }
}
