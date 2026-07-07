package com.consultafacil.api.dto.billing.wallet;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    private String id;
    private String userId;
    private BigDecimal balance;
    private BigDecimal pendingBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
