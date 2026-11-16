package com.consultafacil.api.dto.analytics.professional;

import java.util.Map;

/** average is null when nothing was rated in the period; distribution always has keys 1..5. */
public record RatingStatsDTO(Double average, long total, Map<Integer, Long> distribution) {}
