package com.consultafacil.api.dto.billing.wallet;

import com.consultafacil.domain.enums.WalletTransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionDTO {
    private String id;
    private String walletId;
    private WalletTransactionType type;
    private BigDecimal amount;
    private String description;
    private String referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}
