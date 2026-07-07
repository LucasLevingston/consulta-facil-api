package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.feature.CreateFeatureDTO;
import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;

public interface CreateFeatureUseCase {

    FeatureResponseDTO execute(CreateFeatureDTO dto);
}
