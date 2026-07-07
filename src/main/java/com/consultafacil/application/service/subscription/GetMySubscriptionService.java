package com.consultafacil.application.service.subscription;

import com.consultafacil.api.dto.subscription.SubscriptionResponseDTO;
import com.consultafacil.application.port.in.GetMySubscriptionUseCase;
import com.consultafacil.domain.port.out.SubscriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetMySubscriptionService implements GetMySubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final SubscriptionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<SubscriptionResponseDTO> execute(String userId) {
        return subscriptionRepository.findByUserId(userId).map(mapper::toDTO);
    }
}
