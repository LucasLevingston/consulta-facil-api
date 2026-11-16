package com.consultafacil.api.dto.analytics.professional;

import java.math.BigDecimal;

public record TopServiceDTO(String name, long count, BigDecimal revenue) {}
