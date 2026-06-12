package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.feature.CreateFeatureDTO;
import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.api.dto.billing.feature.UpdateFeatureDTO;

import java.util.List;

public interface FeatureUseCase {
    List<FeatureResponseDTO> listAll();
    FeatureResponseDTO getById(String id);
    FeatureResponseDTO create(CreateFeatureDTO dto);
    FeatureResponseDTO update(String id, UpdateFeatureDTO dto);
    void delete(String id);
}
