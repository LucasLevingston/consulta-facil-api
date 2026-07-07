package com.consultafacil.application.port.in.feature;

import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;

import java.util.List;

public interface ListFeaturesUseCase {

    List<FeatureResponseDTO> execute();
}
