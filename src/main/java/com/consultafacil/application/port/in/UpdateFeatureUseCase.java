package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.api.dto.billing.feature.UpdateFeatureDTO;

public interface UpdateFeatureUseCase {

    FeatureResponseDTO execute(String id, UpdateFeatureDTO dto);
}
