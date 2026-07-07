package com.consultafacil.application.service.feature;

import com.consultafacil.application.port.in.feature.DeleteFeatureUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.feature.FeatureRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteFeatureService implements DeleteFeatureUseCase {

    private final FeatureRepositoryPort featureRepository;

    @Override
    @Transactional
    public void execute(String id) {
        if (!featureRepository.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Feature", id);
        }
        featureRepository.deleteById(id);
    }
}
