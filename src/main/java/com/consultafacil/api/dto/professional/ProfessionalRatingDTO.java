package com.consultafacil.api.dto.professional;

import java.util.Map;

public record ProfessionalRatingDTO(
        Double averageRating,
        long totalRatings,
        Map<Integer, Long> distribution
) {}
