package com.consultafacil.api.dto.analytics.professional;

import java.math.BigDecimal;

/** id is the ProfessionalService id (stable key for the client). */
public record TopServiceDTO(String id, String name, long count, BigDecimal revenue) {}
