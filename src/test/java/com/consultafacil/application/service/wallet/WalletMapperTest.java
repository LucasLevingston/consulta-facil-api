package com.consultafacil.application.service.wallet;

import com.consultafacil.api.dto.billing.wallet.WalletDTO;
import com.consultafacil.domain.entity.Wallet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WalletMapperTest {

    @Test
    void toDTO_copiesEveryField() {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 8, 0);
        Wallet wallet = Wallet.builder()
                .id("w-1").userId("u-1")
                .balance(new BigDecimal("12.34")).pendingBalance(new BigDecimal("5.66"))
                .createdAt(created).updatedAt(created.plusDays(1))
                .build();

        WalletDTO dto = new WalletMapper().toDTO(wallet);

        assertThat(dto.getId()).isEqualTo("w-1");
        assertThat(dto.getUserId()).isEqualTo("u-1");
        assertThat(dto.getBalance()).isEqualByComparingTo("12.34");
        assertThat(dto.getPendingBalance()).isEqualByComparingTo("5.66");
        assertThat(dto.getCreatedAt()).isEqualTo(created);
        assertThat(dto.getUpdatedAt()).isEqualTo(created.plusDays(1));
    }
}
