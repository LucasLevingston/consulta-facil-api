package com.consultafacil.application.port.in.subscription;

import com.consultafacil.api.dto.subscription.SubscriptionResponseDTO;

import java.util.Optional;

public interface GetMySubscriptionUseCase {
    Optional<SubscriptionResponseDTO> execute(String userId);
}
