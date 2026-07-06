package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.wallet.WalletTransactionDTO;
import com.consultafacil.domain.entity.WalletTransaction;
import org.springframework.stereotype.Component;

@Component
public class WalletTransactionMapper {

    public WalletTransactionDTO toDTO(WalletTransaction t) {
        return WalletTransactionDTO.builder()
                .id(t.getId())
                .walletId(t.getWalletId())
                .type(t.getType())
                .amount(t.getAmount())
                .description(t.getDescription())
                .referenceId(t.getReferenceId())
                .referenceType(t.getReferenceType())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
