package com.consultafacil.application.port.in.feature;

import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;

public interface GetFeatureByIdUseCase {

    FeatureResponseDTO execute(String id);
}
