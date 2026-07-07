package com.consultafacil.application.service;

import com.consultafacil.api.dto.subscription.SubscriptionResponseDTO;
import com.consultafacil.domain.entity.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public SubscriptionResponseDTO toDTO(Subscription s) {
        return SubscriptionResponseDTO.builder()
                .id(s.getId())
                .planId(s.getPlanId())
                .status(s.getStatus())
                .expiresAt(s.getExpiresAt())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
