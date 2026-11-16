package com.consultafacil.application.port.in.analytics;

import com.consultafacil.api.dto.analytics.professional.ProfessionalAnalyticsDTO;

public interface GetProfessionalAnalyticsUseCase {
    ProfessionalAnalyticsDTO execute(String userId, String period);
}
