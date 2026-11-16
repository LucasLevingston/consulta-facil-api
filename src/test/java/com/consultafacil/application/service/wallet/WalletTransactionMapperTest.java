package com.consultafacil.application.service.wallet;

import com.consultafacil.api.dto.billing.wallet.WalletTransactionDTO;
import com.consultafacil.domain.entity.WalletTransaction;
import com.consultafacil.domain.enums.WalletTransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WalletTransactionMapperTest {

    @Test
    void toDTO_copiesEveryField() {
        LocalDateTime created = LocalDateTime.of(2026, 2, 3, 4, 5);
        WalletTransaction tx = WalletTransaction.builder()
                .id("t-1").walletId("w-1").type(WalletTransactionType.ADJUSTMENT)
                .amount(new BigDecimal("2.50")).description("Ajuste manual")
                .referenceId("ref").referenceType("MANUAL").createdAt(created)
                .build();

        WalletTransactionDTO dto = new WalletTransactionMapper().toDTO(tx);

        assertThat(dto.getId()).isEqualTo("t-1");
        assertThat(dto.getWalletId()).isEqualTo("w-1");
        assertThat(dto.getType()).isEqualTo(WalletTransactionType.ADJUSTMENT);
        assertThat(dto.getAmount()).isEqualByComparingTo("2.50");
        assertThat(dto.getDescription()).isEqualTo("Ajuste manual");
        assertThat(dto.getReferenceId()).isEqualTo("ref");
        assertThat(dto.getReferenceType()).isEqualTo("MANUAL");
        assertThat(dto.getCreatedAt()).isEqualTo(created);
    }
}
